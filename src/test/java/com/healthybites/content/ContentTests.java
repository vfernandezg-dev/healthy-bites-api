package com.healthybites.content;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;

/** Contenido: acceso por nivel o desbloqueo, ficheros protegidos con enlace firmado y el espacio del autor. */
class ContentTests extends IntegrationTest {

    private static final byte[] PNG = {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'
    };
    private static final byte[] PDF = "%PDF-1.7\n1 0 obj\n".getBytes(StandardCharsets.ISO_8859_1);

    @Autowired
    private ContentAccess contentAccess;

    @Test
    void theCatalogMarksLockedItemsAndHonoursUnlocksAndFilters() throws Exception {
        String juan = bearer(token(Seed.JUAN));
        mockMvc.perform(get("/content-items").param("size", "50").header(HttpHeaders.AUTHORIZATION, juan))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.content[?(@.slug == 'como-interpretar-tu-composicion-corporal')].locked", contains(true)))
                .andExpect(jsonPath("$.content[?(@.slug == 'guia-de-alimentacion-saludable')].locked", contains(false)))
                // Desbloqueada con una racha aunque sea PREMIUM.
                .andExpect(jsonPath(
                        "$.content[?(@.slug == 'rutina-de-fuerza-para-principiantes')].locked", contains(false)))
                .andExpect(jsonPath("$.content[*].slug", not(hasItem("meal-prep-de-domingo"))));

        mockMvc.perform(get("/content-items").param("type", "VIDEO").header(HttpHeaders.AUTHORIZATION, juan))
                .andExpect(jsonPath("$.content[*].type", everyItem(is("VIDEO"))));
        mockMvc.perform(get("/content-items").param("q", "proteína").header(HttpHeaders.AUTHORIZATION, juan))
                .andExpect(jsonPath("$.content[*].slug", contains("recetas-altas-en-proteina")));
        mockMvc.perform(get("/me/content-unlocks").header(HttpHeaders.AUTHORIZATION, juan))
                .andExpect(jsonPath("$[*].slug", hasItem("rutina-de-fuerza-para-principiantes")));
    }

    @Test
    void lockedItemsAre403WithTheRequiredTier() throws Exception {
        detail("como-interpretar-tu-composicion-corporal", Seed.JUAN)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("subscription-required"))
                .andExpect(jsonPath("$.requiredTier").value("PREMIUM"));
        detail("como-interpretar-tu-composicion-corporal", Seed.MARIA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body", startsWith("El IMC")));
        detail("recetas-altas-en-proteina", Seed.MARIA).andExpect(status().isForbidden());
        detail("recetas-altas-en-proteina", Seed.PEDRO).andExpect(status().isOk());
        detail("meal-prep-de-domingo", Seed.PEDRO).andExpect(status().isNotFound());
    }

    @Test
    void anAuthorPublishesContentWithCoverAndAProtectedFile() throws Exception {
        String carlos = bearer(token(Seed.CARLOS));
        String cover = upload(carlos, "CONTENT_COVER", PNG, "portada.png");
        String file = upload(carlos, "CONTENT_FILE", PDF, "guia.pdf");

        String created = mockMvc.perform(post("/nutritionist/content-items")
                        .header(HttpHeaders.AUTHORIZATION, carlos)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody("Guía de la compra", "PREMIUM", cover, file)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.item.slug").value("guia-de-la-compra"))
                .andExpect(jsonPath("$.item.coverPath").value("/media/" + cover))
                .andExpect(jsonPath("$.item.hasFile").value(true))
                // El autor recibe los ids para reenviarlos al editar; el detalle público no los lleva.
                .andExpect(jsonPath("$.coverMediaId").value(cover))
                .andExpect(jsonPath("$.fileMediaId").value(file))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = JsonPath.read(created, "$.item.id");
        mockMvc.perform(post("/nutritionist/content-items/" + id + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, carlos))
                .andExpect(jsonPath("$.item.status").value("PUBLISHED"));

        // La portada es pública; el fichero, solo con enlace firmado y si se puede abrir el contenido.
        mockMvc.perform(get("/media/" + cover)).andExpect(status().isOk());
        mockMvc.perform(get("/media/" + file)).andExpect(status().isNotFound());
        mockMvc.perform(get("/content-items/" + id + "/file")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isForbidden());
        String link = JsonPath.read(
                mockMvc.perform(get("/content-items/" + id + "/file")
                                .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA))))
                        .andExpect(status().isOk())
                        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.path");
        mockMvc.perform(get(link))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"));

        // Con el mismo título, otro slug.
        mockMvc.perform(post("/nutritionist/content-items")
                        .header(HttpHeaders.AUTHORIZATION, carlos)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody("Guía de la compra", "BASIC", null, null)))
                .andExpect(jsonPath("$.item.slug").value("guia-de-la-compra-2"));
    }

    @Test
    void mediaMustBeTheAuthorsOwnAndOfTheRightPurpose() throws Exception {
        String carlosCover = upload(bearer(token(Seed.CARLOS)), "CONTENT_COVER", PNG, "a.png");
        String postImage = upload(bearer(token(Seed.ELENA)), "POST_IMAGE", PNG, "b.png");

        mockMvc.perform(post("/nutritionist/content-items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ELENA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody("Portada ajena", "BASIC", carlosCover, null)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("invalid-media"));
        mockMvc.perform(post("/nutritionist/content-items")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ELENA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentBody("Propósito equivocado", "BASIC", postImage, null)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void aRewardUnlocksPaidContent() throws Exception {
        NewUser client = newClient("contenido");
        detail("recetas-altas-en-proteina", client.token(), true).andExpect(status().isForbidden());

        contentAccess.grant(UUID.fromString("00000000-0000-7000-8000-0000000a0006"), client.id());

        detail("recetas-altas-en-proteina", client.token(), true).andExpect(status().isOk());
    }

    private ResultActions detail(String slug, String email) throws Exception {
        return detail(slug, token(email), true);
    }

    private ResultActions detail(String slug, String accessToken, boolean ignored) throws Exception {
        return mockMvc.perform(get("/content-items/" + slug).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)));
    }

    private String upload(String auth, String purpose, byte[] bytes, String name) throws Exception {
        String body = mockMvc.perform(multipart("/media")
                        .file(new MockMultipartFile("file", name, "application/octet-stream", bytes))
                        .param("purpose", purpose)
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private static String contentBody(String title, String tier, String cover, String file) {
        return """
                {"title": "%s", "summary": "Resumen de prueba", "body": "Texto", "type": "GUIDE",
                 "category": "NUTRITION", "requiredTier": "%s", "coverMediaId": %s, "fileMediaId": %s}
                """.formatted(
                title, tier, cover == null ? "null" : "\"" + cover + "\"", file == null ? "null" : "\"" + file + "\"");
    }
}
