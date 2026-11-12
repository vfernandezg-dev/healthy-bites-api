package com.healthybites.media;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Ficheros: tipo por los bytes, nombre del servidor, públicos por id y protegidos solo con URL firmada; siempre con
 * cabeceras que impiden que el navegador ejecute nada.
 */
class MediaTests extends IntegrationTest {

    private static final byte[] PNG = {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'
    };
    private static final byte[] PDF = "%PDF-1.7\n1 0 obj\n".getBytes(StandardCharsets.ISO_8859_1);

    @Autowired
    private MediaFiles mediaFiles;

    @Test
    void aPublicImageIsServedByIdWithSafeHeaders() throws Exception {
        // El navegador dice text/html y el nombre acaba en .html: cuenta lo que hay en los bytes (un PNG).
        String body = upload(Seed.JUAN, "POST_IMAGE", "foto.html", "text/html", PNG)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String path = JsonPath.read(body, "$.path");

        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("inline")))
                .andExpect(header().string("Content-Security-Policy", containsString("sandbox")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("immutable")))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void scriptsDisguisedAsImagesAreRejected() throws Exception {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);
        byte[] html = "<!doctype html><script>alert(document.cookie)</script>".getBytes(StandardCharsets.UTF_8);

        upload(Seed.CARLOS, "CONTENT_COVER", "logo.png", "image/png", svg)
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("unsupported-file-type"));
        upload(Seed.CARLOS, "CONTENT_FILE", "pagina.pdf", "application/pdf", html)
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void clientsOnlyUploadPostImages() throws Exception {
        upload(Seed.JUAN, "CONTENT_FILE", "guia.pdf", "application/pdf", PDF)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("media-purpose-forbidden"));
        upload(Seed.JUAN, "POST_IMAGE", "guia.pdf", "application/pdf", PDF)
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void protectedFilesNeedAValidSignedLink() throws Exception {
        String body = upload(Seed.CARLOS, "CONTENT_FILE", "guia.pdf", "application/pdf", PDF)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID id = UUID.fromString(JsonPath.read(body, "$.id"));

        mockMvc.perform(get("/media/" + id)).andExpect(status().isNotFound());

        String signed = mediaFiles.signedPath(id, Duration.ofMinutes(5));
        mockMvc.perform(get(signed))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, startsWith("attachment")));

        mockMvc.perform(get(signed.replaceAll("signature=.", "signature=x"))).andExpect(status().isNotFound());
        mockMvc.perform(get(mediaFiles.signedPath(id, Duration.ofMinutes(-1)))).andExpect(status().isNotFound());
        // La firma es de ese fichero: no abre otro.
        mockMvc.perform(get(signed.replace(id.toString(), UUID.randomUUID().toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void imagesOverTheirLimitAreRejected() throws Exception {
        byte[] big = Arrays.copyOf(PNG, 5 * 1024 * 1024 + 1);

        upload(Seed.JUAN, "POST_IMAGE", "grande.png", "image/png", big)
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.code").value("file-too-large"));
    }

    @Test
    void unknownFilesAreNotFoundAndUploadingNeedsASession() throws Exception {
        mockMvc.perform(get("/media/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get("/media/no-es-un-uuid")).andExpect(status().isBadRequest());
        mockMvc.perform(multipart("/media")
                        .file(new MockMultipartFile("file", "a.png", "image/png", PNG))
                        .param("purpose", "POST_IMAGE"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions upload(String email, String purpose, String name, String contentType, byte[] bytes)
            throws Exception {
        return mockMvc.perform(multipart("/media")
                .file(new MockMultipartFile("file", name, contentType, bytes))
                .param("purpose", purpose)
                .header(HttpHeaders.AUTHORIZATION, bearer(token(email))));
    }
}
