package com.healthybites.media.infra;

import com.healthybites.media.app.FileStorage;
import com.healthybites.shared.web.ApiException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Almacenamiento de objetos S3-compatible (Cloudflare R2, AWS S3): lo comparten todas las instancias y no se
 * pierde al reiniciar un contenedor.
 *
 * <ul>
 *   <li>Los ficheros se leen con URLs prefirmadas que el navegador pide directamente al almacenamiento: los vídeos y
 *       PDF no pasan por la API y el contenido subido por usuarios se sirve desde otro origen.</li>
 *   <li>Sumas de comprobación solo cuando S3 las exige: no todos los compatibles aceptan las nuevas por defecto.</li>
 *   <li>Un fallo del almacenamiento llega al cliente como 503 {@code storage-unavailable}.</li>
 * </ul>
 */
@Slf4j
final class S3FileStorage implements FileStorage, AutoCloseable {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;

    S3FileStorage(S3Client s3, S3Presigner presigner, String bucket) {
        this.s3 = s3;
        this.presigner = presigner;
        this.bucket = bucket;
    }

    static S3FileStorage create(StorageProperties.S3 config) {
        URI endpoint = URI.create(required(config.endpoint(), "S3_ENDPOINT"));
        String bucket = required(config.bucket(), "S3_BUCKET");
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(
                required(config.accessKey(), "S3_ACCESS_KEY"), required(config.secretKey(), "S3_SECRET_KEY")));
        Region region = Region.of(config.region());
        S3Configuration service = S3Configuration.builder()
                .pathStyleAccessEnabled(config.pathStyle())
                .build();
        S3Client client = S3Client.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentials)
                .serviceConfiguration(service)
                .httpClient(UrlConnectionHttpClient.create())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentials)
                .serviceConfiguration(service)
                .build();
        return new S3FileStorage(client, presigner, bucket);
    }

    @Override
    public void store(InputStream content, long size, String contentType, String key) {
        call(() -> s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .contentLength(size)
                        .build(),
                RequestBody.fromInputStream(content, size)));
    }

    @Override
    public Optional<Resource> load(String key) {
        try {
            return Optional.of(new InputStreamResource(call(() -> s3.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(key).build()))));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        call(() -> s3.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(key).build()));
    }

    /** Se firma en local, sin llamar al almacenamiento. */
    @Override
    public Optional<URI> presignedGet(String key, DirectRead read) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentType(read.contentType())
                .responseContentDisposition(read.contentDisposition())
                .responseCacheControl(read.cacheControl())
                .build();
        return Optional.of(URI.create(presigner
                .presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(read.validFor())
                        .getObjectRequest(request)
                        .build())
                .url()
                .toString()));
    }

    @Override
    public void close() {
        presigner.close();
        s3.close();
    }

    private static <T> T call(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (NoSuchKeyException e) {
            throw e;
        } catch (SdkException e) {
            log.warn("Fallo del almacenamiento de objetos: {}", e.getMessage());
            throw ApiException.serviceUnavailable(
                    "storage-unavailable", "No pudimos acceder a los ficheros en este momento. Inténtalo más tarde.");
        }
    }

    private static String required(@Nullable String value, String variable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Con STORAGE_TYPE=s3 hace falta " + variable);
        }
        return value.trim();
    }
}
