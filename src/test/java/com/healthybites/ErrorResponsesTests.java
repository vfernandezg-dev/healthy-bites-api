package com.healthybites;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Formato RFC 9457 (ProblemDetail) de los errores de la API. */
class ErrorResponsesTests extends IntegrationTest {

    @Test
    void requestWithoutSessionGetsAProblemDetail401() throws Exception {
        mockMvc.perform(get("/meta/cliente/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").isNotEmpty())
                // Compatibilidad temporal con el web-app actual
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void businessValidationErrorsAre400WithTheirMessage() throws Exception {
        // Antes: IllegalArgumentException -> 500
        mockMvc.perform(post("/auth/register/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Otra",
                                  "apellido": "Persona",
                                  "correo": "juan.perez@healthybites.com",
                                  "contrasena": "healthy123",
                                  "sexo": "Otro",
                                  "edad": 30,
                                  "altura": 1.70,
                                  "peso": 70.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El correo ya está registrado"))
                .andExpect(jsonPath("$.message").value("El correo ya está registrado"));
    }

    @Test
    void beanValidationErrorsAre422WithFieldErrors() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "no-es-un-correo", "contrasena": ""}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors[*].field").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }
}
