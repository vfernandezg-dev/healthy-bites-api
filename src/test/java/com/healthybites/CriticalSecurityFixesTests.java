package com.healthybites;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regresiones de los agujeros críticos cerrados en la fase de seguridad (API-4). Cada test reproduce el
 * ataque que antes funcionaba.
 */
class CriticalSecurityFixesTests extends IntegrationTest {

    @Test
    void registrationIgnoresAClientSuppliedIdAndCannotOverwriteTheAdmin() throws Exception {
        // Antes: el "id" se copiaba al usuario y save() sobrescribía la cuenta 1 (el ADMIN del seed).
        mockMvc.perform(post("/auth/register/cliente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 1,
                                  "nombre": "Intruso",
                                  "apellido": "Prueba",
                                  "correo": "intruso@example.com",
                                  "contrasena": "healthy123",
                                  "sexo": "Otro",
                                  "edad": 30,
                                  "altura": 1.70,
                                  "peso": 70.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(not(1)));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("admin@healthybites.com", DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("intruso@example.com", DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    void profileUpdateIgnoresAClientSuppliedProfileId() throws Exception {
        // Juan es el usuario 4 y su perfil de cliente es el 1. Antes, el "id" del cuerpo se asignaba al perfil.
        String token = loginAndGetToken("juan.perez@healthybites.com");

        mockMvc.perform(put("/user/profile/4")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 2,
                                  "nombre": "Juan",
                                  "apellido": "Pérez",
                                  "sexo": "Masculino",
                                  "edad": 28,
                                  "altura": 1.75,
                                  "peso": 71.5
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("juan.perez@healthybites.com", DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(1));
    }

    @Test
    void onlyAnAdminCanRegisterNutritionists() throws Exception {
        String body = """
                {
                  "nombre": "Nueva",
                  "apellido": "Nutricionista",
                  "correo": "nueva.nutricionista@example.com",
                  "contrasena": "healthy123",
                  "bio": "Alta hecha por la administración."
                }
                """;

        mockMvc.perform(post("/auth/register/nutricionista").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        String clientToken = loginAndGetToken("maria.lopez@healthybites.com");
        mockMvc.perform(post("/auth/register/nutricionista")
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken("admin@healthybites.com");
        mockMvc.perform(post("/auth/register/nutricionista")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void clientsCannotConfirmListOrDeleteSubscriptions() throws Exception {
        // Antes un cliente marcaba su suscripción como pagada sin pasar por PayPal.
        String clientToken = loginAndGetToken("juan.perez@healthybites.com");

        mockMvc.perform(put("/suscripcion/confirm/1").header("Authorization", bearer(clientToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/suscripcion").header("Authorization", bearer(clientToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/suscripcion/2").header("Authorization", bearer(clientToken)))
                .andExpect(status().isForbidden());

        String adminToken = loginAndGetToken("admin@healthybites.com");
        mockMvc.perform(get("/suscripcion").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void uploadingMediaRequiresASession() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "file", "foto.png", "image/png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13});

        mockMvc.perform(multipart("/media/upload").file(image))
                .andExpect(status().isUnauthorized());

        String token = loginAndGetToken("maria.lopez@healthybites.com");
        mockMvc.perform(multipart("/media/upload").file(image).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.path").isNotEmpty());
    }
}
