package com.healthybites.media.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthybites.media.app.FileStorage.DirectRead;
import com.healthybites.shared.web.ApiException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/** El almacenamiento S3-compatible contra un servidor S3 real en un contenedor (Adobe S3Mock). */
@Testcontainers
class S3FileStorageTests {

    private static final String BUCKET = "healthybites-test";

    @Container
    private static final GenericContainer<?> S3 = new GenericContainer<>(DockerImageName.parse("adobe/s3mock:5.2.3"))
            .withExposedPorts(9090)
            .waitingFor(Wait.forHttp("/").forPort(9090));

    private static S3FileStorage storage;

    @BeforeAll
    static void createBucket() {
        try (S3Client admin = S3Client.builder()
                .endpointOverride(URI.create(endpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .forcePathStyle(true)
                .build()) {
            admin.createBucket(request -> request.bucket(BUCKET));
        }
        storage = S3FileStorage.create(config(endpoint()));
    }

    @AfterAll
    static void close() {
        storage.close();
    }

    @Test
    void storesReadsThroughAPresignedUrlAndDeletes() throws Exception {
        byte[] content = "%PDF-1.7 guía de recetas".getBytes(StandardCharsets.UTF_8);
        storage.store(new ByteArrayInputStream(content), content.length, "application/pdf", "guia.pdf");

        Resource loaded = storage.load("guia.pdf").orElseThrow();
        try (var in = loaded.getInputStream()) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }

        URI url = storage.presignedGet(
                        "guia.pdf",
                        new DirectRead(
                                "application/pdf",
                                "attachment; filename=\"guia.pdf\"",
                                "private, max-age=300",
                                Duration.ofMinutes(5)))
                .orElseThrow();
        assertThat(url.getQuery()).contains("X-Amz-Signature", "X-Amz-Expires=300", "response-content-disposition");
        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(url).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo(content);

        storage.delete("guia.pdf");
        assertThat(storage.load("guia.pdf")).isEmpty();
    }

    @Test
    void theBrowserUploadsWithAPresignedPutAndTheApiMovesItToItsFinalKey() throws Exception {
        byte[] content = "%PDF-1.7 subida directa".getBytes(StandardCharsets.UTF_8);
        URI url = storage.presignedPut("pending-guia.pdf", "application/pdf", content.length, Duration.ofMinutes(15))
                .orElseThrow();
        assertThat(url.getQuery()).contains("X-Amz-Signature", "X-Amz-Expires=900");

        // Lo que hace el navegador: un PUT con el tipo firmado.
        HttpResponse<Void> put = HttpClient.newHttpClient()
                .send(
                        HttpRequest.newBuilder(url)
                                .header("Content-Type", "application/pdf")
                                .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
                                .build(),
                        HttpResponse.BodyHandlers.discarding());
        assertThat(put.statusCode()).isEqualTo(200);

        storage.move("pending-guia.pdf", "guia-final.pdf");

        assertThat(storage.load("pending-guia.pdf")).isEmpty();
        try (var in = storage.load("guia-final.pdf").orElseThrow().getInputStream()) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }
        storage.delete("guia-final.pdf");
    }

    @Test
    void anUnreachableStorageAnswers503() {
        try (S3FileStorage down = S3FileStorage.create(config("http://127.0.0.1:1"))) {
            assertThatThrownBy(() -> down.store(new ByteArrayInputStream(new byte[] {1}), 1, "image/png", "x.png"))
                    .isInstanceOfSatisfying(ApiException.class, error -> {
                        assertThat(error.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                        assertThat(error.getCode()).isEqualTo("storage-unavailable");
                    });
        }
    }

    @Test
    void withoutItsSettingsTheApplicationDoesNotStart() {
        assertThatThrownBy(() -> S3FileStorage.create(new StorageProperties.S3(null, BUCKET, "k", "s", "auto", true)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("S3_ENDPOINT");
    }

    private static String endpoint() {
        return "http://" + S3.getHost() + ":" + S3.getMappedPort(9090);
    }

    private static StorageProperties.S3 config(String endpoint) {
        return new StorageProperties.S3(endpoint, BUCKET, "test", "test", "us-east-1", true);
    }
}
