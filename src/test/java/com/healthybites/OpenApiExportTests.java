package com.healthybites;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Exporta el contrato OpenAPI a {@code docs/openapi.yaml}. El web-app genera sus tipos a partir de este fichero y
 * CI falla si, tras los tests, difiere del commiteado: cualquier cambio en la API queda a la vista en el PR.
 */
class OpenApiExportTests extends IntegrationTest {

    static final Path EXPORT = Path.of("docs", "openapi.yaml");

    @Test
    void exportsTheApiContract() throws Exception {
        String yaml = mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(yaml).contains("openapi:").contains("/auth/login").contains("/contact");
        Files.createDirectories(EXPORT.getParent());
        Files.writeString(EXPORT, yaml.endsWith("\n") ? yaml : yaml + "\n", StandardCharsets.UTF_8);
    }

    /** Lo que no es {@code @Nullable} es obligatorio; lo {@code @Nullable} admite null (ver NullnessModelConverter). */
    @Test
    void theContractCarriesRequiredAndNullableFields() throws Exception {
        String schemas = "$.components.schemas.";
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        schemas + "SessionResponse.required",
                        contains("accessToken", "expiresIn", "tokenType", "user")))
                .andExpect(
                        jsonPath(schemas + "GroupResponse.required", hasItems("id", "name", "visibility", "members")))
                .andExpect(jsonPath(schemas + "GroupResponse.required", not(hasItem("description"))))
                .andExpect(jsonPath(schemas + "GroupResponse.properties.description.type", hasItems("string", "null")))
                .andExpect(jsonPath(schemas + "GroupResponse.properties.membership.oneOf[1].type")
                        .value("null"))
                // El contrato con la landing no cambia: los campos siguen en español y el honeypot es opcional.
                .andExpect(jsonPath(
                        schemas + "ContactMessage.required", contains("apellido", "correo", "mensaje", "nombre")))
                .andExpect(jsonPath(schemas + "PagedModelGroupResponse.required", contains("content", "page")))
                .andExpect(jsonPath(
                        schemas + "PageMetadata.required", hasItems("number", "size", "totalElements", "totalPages")));
    }
}
