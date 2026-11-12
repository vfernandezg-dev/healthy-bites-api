package com.healthybites.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.identity.app.InvitationIssued;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.ResultActions;

/** Alta de nutricionistas: solo por invitación de un ADMIN, con un enlace de un solo uso. */
@RecordApplicationEvents
class InvitationTests extends IntegrationTest {

    private static final String PASSWORD = "Nutri-Clave-2026";

    @org.springframework.beans.factory.annotation.Autowired
    private ApplicationEvents events;

    @Test
    void anInvitedNutritionistCreatesTheirAccountWithTheLinkOnce() throws Exception {
        String email = "nutri-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        invite(email)
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.status").value("PENDING"));

        String link = singleInvitation().link();
        assertThat(link).startsWith("http://localhost:4210/auth/accept-invitation#token=");
        String token = link.substring(link.indexOf("#token=") + 7);

        mockMvc.perform(post("/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenBody(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value("Lucía"));

        accept(token, "Dietista deportiva.")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("NUTRITIONIST"))
                .andExpect(jsonPath("$.nutritionist.bio").value("Dietista deportiva."));

        String accessToken = login(email, PASSWORD);
        mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(jsonPath("$.role").value("NUTRITIONIST"));

        accept(token, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid-invitation"));
    }

    @Test
    void resendingInvalidatesThePreviousLinkAndRevokingClosesIt() throws Exception {
        String email = "reenvio-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String id = JsonPath.read(invite(email).andReturn().getResponse().getContentAsString(), "$.id");
        mockMvc.perform(post("/admin/nutritionist-invitations/" + id + "/resend")
                        .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk());
        List<InvitationIssued> sent = events.stream(InvitationIssued.class).toList();
        assertThat(sent).hasSize(2);

        mockMvc.perform(post("/auth/invitations/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenBody(tokenOf(sent.get(0)))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/admin/nutritionist-invitations/" + id).header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNoContent());
        accept(tokenOf(sent.get(1)), null).andExpect(status().isBadRequest());

        mockMvc.perform(get("/admin/nutritionist-invitations").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + id + "')].status")
                        .value(org.hamcrest.Matchers.contains("REVOKED")));
    }

    @Test
    void existingAccountsAndDuplicatePendingInvitationsAreConflicts() throws Exception {
        invite(Seed.JUAN)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("email-taken"));

        String email = "doble-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        invite(email).andExpect(status().isCreated());
        invite(email.toUpperCase())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invitation-pending"));
    }

    @Test
    void nutritionistsCannotRegisterThemselvesOrInviteOthers() throws Exception {
        mockMvc.perform(post("/admin/nutritionist-invitations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.CARLOS)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inviteBody("otro@example.com")))
                .andExpect(status().isForbidden());
    }

    private ResultActions invite(String email) throws Exception {
        return mockMvc.perform(post("/admin/nutritionist-invitations")
                .header(HttpHeaders.AUTHORIZATION, admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(inviteBody(email)));
    }

    private ResultActions accept(String token, String bio) throws Exception {
        String bioJson = bio == null ? "null" : "\"" + bio + "\"";
        return mockMvc.perform(post("/auth/invitations/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token": "%s", "password": "%s", "bio": %s}
                        """.formatted(token, PASSWORD, bioJson)));
    }

    private String admin() throws Exception {
        return bearer(token(Seed.ADMIN));
    }

    private InvitationIssued singleInvitation() {
        List<InvitationIssued> sent = events.stream(InvitationIssued.class).toList();
        assertThat(sent).hasSize(1);
        return sent.getFirst();
    }

    private static String tokenOf(InvitationIssued event) {
        return event.link().substring(event.link().indexOf("#token=") + 7);
    }

    private static String tokenBody(String token) {
        return "{\"token\": \"" + token + "\"}";
    }

    private static String inviteBody(String email) {
        return """
                {"email": "%s", "firstName": "Lucía", "lastName": "Ramos"}
                """.formatted(email);
    }
}
