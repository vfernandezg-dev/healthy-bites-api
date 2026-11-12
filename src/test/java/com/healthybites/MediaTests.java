package com.healthybites;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ficheros subidos (API-8): tipo detectado por sus bytes, nombres del servidor y solo se sirve lo registrado,
 * con cabeceras que impiden que el navegador ejecute nada.
 */
class MediaTests extends IntegrationTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};
    private static final byte[] PDF = "%PDF-1.7\n%âãÏÓ\n1 0 obj\n".getBytes(StandardCharsets.ISO_8859_1);

    @Value("${healthybites.storage.location}")
    private String storageLocation;

    @Test
    void anImageIsStoredUnderAServerNameAndServedWithSafeHeaders() throws Exception {
        // El navegador dice "text/html" y el nombre acaba en .html: cuenta lo que hay en los bytes (un PNG).
        String path = uploadPath(upload("juan.perez@healthybites.com", "foto.html", "text/html", PNG)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path", matchesPattern("[0-9a-f-]{36}\\.png"))));

        mockMvc.perform(get("/media/" + path))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("inline")))
                .andExpect(header().string("Content-Security-Policy", containsString("sandbox")))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void scriptsDisguisedAsImagesAreRejected() throws Exception {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);
        byte[] html = "<!doctype html><script>alert(document.cookie)</script>".getBytes(StandardCharsets.UTF_8);

        upload("carlos.garcia@healthybites.com", "logo.png", "image/png", svg).andExpect(status().isUnsupportedMediaType());
        upload("carlos.garcia@healthybites.com", "logo.svg", "image/svg+xml", svg).andExpect(status().isUnsupportedMediaType());
        upload("carlos.garcia@healthybites.com", "foto.jpg", "image/jpeg", html).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void clientsUploadOnlyImagesWhileNutritionistsAlsoUploadDocuments() throws Exception {
        upload("juan.perez@healthybites.com", "guia.pdf", "application/pdf", PDF)
                .andExpect(status().isUnsupportedMediaType());

        String path = uploadPath(upload("carlos.garcia@healthybites.com", "guia.pdf", "application/pdf", PDF)
                .andExpect(status().isOk()));
        mockMvc.perform(get("/media/" + path))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment")));
    }

    @Test
    void imagesOverTheirLimitAreRejected() throws Exception {
        byte[] big = Arrays.copyOf(PNG, 5 * 1024 * 1024 + 1);

        upload("juan.perez@healthybites.com", "grande.png", "image/png", big).andExpect(status().isContentTooLarge());
    }

    @Test
    void onlyRegisteredFilesAreServed() throws Exception {
        // Un fichero que está en disco pero no se subió por la API (o cuyo registro se borró) no existe.
        String stray = UUID.randomUUID() + ".png";
        Path dir = Path.of(storageLocation);
        Files.createDirectories(dir);
        Files.write(dir.resolve(stray), PNG);

        mockMvc.perform(get("/media/" + stray)).andExpect(status().isNotFound());
        mockMvc.perform(get("/media/application.yml")).andExpect(status().isNotFound());
        mockMvc.perform(get("/media/null")).andExpect(status().isNotFound());
    }

    private ResultActions upload(String email, String name, String contentType, byte[] bytes) throws Exception {
        return mockMvc.perform(multipart("/media/upload")
                .file(new MockMultipartFile("file", name, contentType, bytes))
                .header("Authorization", bearer(loginAndGetToken(email))));
    }

    private static String uploadPath(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.path");
    }
}
