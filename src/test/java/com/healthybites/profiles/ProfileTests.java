package com.healthybites.profiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.support.Seed;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/** Registro público de clientes, {@code /me} y cambio de contraseña. */
class ProfileTests extends IntegrationTest {

    @Test
    void aClientRegistersWithTheirProfileAndThenLogsIn() throws Exception {
        String email = "nuevo-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        register(email, """
                        {"sex": "FEMALE", "birthDate": "1996-05-20", "heightCm": 168.5, "weightKg": 64.2}""")
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/me"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.client.heightCm").value(168.5))
                .andExpect(jsonPath("$.client.age", Matchers.greaterThanOrEqualTo(29)));

        String token = login(email, "Clave-Segura-1");
        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.client.sex").value("FEMALE"));
    }

    @Test
    void anEmailCanOnlyBeRegisteredOnceIgnoringCase() throws Exception {
        register("JUAN.PEREZ@healthybites.com", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("email-taken"));
    }

    @Test
    void invalidRegistrationsAre422WithFieldErrors() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "no-es-correo", "password": "corta", "firstName": "", "lastName": "X",
                                 "profile": {"heightCm": 20, "birthDate": "2999-01-01"}}"""))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath(
                        "$.errors[*].field",
                        Matchers.hasItems("email", "password", "firstName", "profile.heightCm", "profile.birthDate")));
    }

    @Test
    void meShowsTheProfileOfEachRole() throws Exception {
        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA))))
                .andExpect(jsonPath("$.id").value(Seed.MARIA_ID.toString()))
                .andExpect(jsonPath("$.client.heightCm").value(162.0))
                .andExpect(jsonPath("$.nutritionist").doesNotExist());
        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.CARLOS))))
                .andExpect(jsonPath("$.role").value("NUTRITIONIST"))
                .andExpect(jsonPath("$.nutritionist.bio", Matchers.startsWith("Nutricionista clínico")))
                .andExpect(jsonPath("$.client").doesNotExist());
        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updatingTheProfileReplacesNamesAndRoleData() throws Exception {
        String email = "editar-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        register(email, null);
        String token = login(email, "Clave-Segura-1");

        mockMvc.perform(put("/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Lola", "lastName": "Ruiz",
                                 "client": {"sex": "OTHER", "heightCm": 170, "weightKg": 70.5},
                                 "bio": "ignorada para un cliente"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Lola"))
                .andExpect(jsonPath("$.client.sex").value("OTHER"))
                .andExpect(jsonPath("$.client.weightKg").value(70.5))
                .andExpect(jsonPath("$.nutritionist").doesNotExist());
    }

    @Test
    void changingThePasswordNeedsTheCurrentOneAndOpensANewSession() throws Exception {
        String email = "clave-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        register(email, null);
        String token = login(email, "Clave-Segura-1");

        mockMvc.perform(put("/me/password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\": \"incorrecta\", \"newPassword\": \"Otra-Clave-2026\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("wrong-current-password"))
                .andExpect(jsonPath("$.errors[0].field").value("currentPassword"));

        mockMvc.perform(put("/me/password")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\": \"Clave-Segura-1\", \"newPassword\": \"Otra-Clave-2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("HttpOnly")));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "Clave-Segura-1")))
                .andExpect(status().isUnauthorized());
        login(email, "Otra-Clave-2026");
    }

    private ResultActions register(String email, String profileJson) throws Exception {
        return mockMvc.perform(
                post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "%s", "password": "Clave-Segura-1", "firstName": "Nueva", "lastName": "Clienta",
                         "profile": %s}
                        """.formatted(
                                email, profileJson == null ? "null" : profileJson)));
    }
}
