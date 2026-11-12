package com.healthybites;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
