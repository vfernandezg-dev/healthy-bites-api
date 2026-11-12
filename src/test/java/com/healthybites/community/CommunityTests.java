package com.healthybites.community;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.community.events.MembershipRequested;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.ResultActions;

/** Comunidad: grupos privados solo para miembros, participación de miembros activos y moderación. */
@RecordApplicationEvents
class CommunityTests extends IntegrationTest {

    // Seed: G1 y G2 públicos (de Juan y María), G3 privado de Pedro (María tiene la solicitud pendiente).
    private static final String G1 = "00000000-0000-7000-8000-000000003001";
    private static final String G3 = "00000000-0000-7000-8000-000000003003";
    private static final String MARIA_POST_IN_G1 = "00000000-0000-7000-8000-000000003202";

    @Autowired
    private ApplicationEvents events;

    @Test
    void privateGroupsAreListedButOnlyMembersReadThem() throws Exception {
        mockMvc.perform(get("/groups").param("size", "50").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(jsonPath("$.content[?(@.id == '" + G3 + "')].visibility", contains("PRIVATE")))
                .andExpect(jsonPath("$.content[?(@.id == '" + G1 + "')].membership.role", contains("OWNER")));
        posts(G3, token(Seed.JUAN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("private-group"));
        posts(G3, token(Seed.MARIA)).andExpect(status().isForbidden()); // pendiente
        posts(G3, token(Seed.PEDRO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
        posts(G1, token(Seed.PEDRO)).andExpect(status().isOk());
    }

    @Test
    void joiningAPrivateGroupWaitsForTheOwnerToApprove() throws Exception {
        NewUser client = newClient("comunidad");
        String auth = bearer(client.token());

        join(auth, G3)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membership.status").value("PENDING"));
        join(auth, G3).andExpect(status().isConflict());
        assertThat(events.stream(MembershipRequested.class))
                .anyMatch(e -> e.requesterId().equals(client.id()));
        posts(G3, client.token()).andExpect(status().isForbidden());

        String pedro = bearer(token(Seed.PEDRO));
        mockMvc.perform(get("/groups/" + G3 + "/members").header(HttpHeaders.AUTHORIZATION, pedro))
                .andExpect(jsonPath("$[*].status", hasItem("PENDING")));
        mockMvc.perform(post("/groups/" + G3 + "/members/" + client.id() + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, pedro))
                .andExpect(status().isNoContent());
        posts(G3, client.token()).andExpect(status().isOk());
        mockMvc.perform(post("/groups/" + G3 + "/members/" + client.id() + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("not-group-owner"));
    }

    @Test
    void onlyActiveMembersPostAndCommentAndAuthorsOrOwnersDelete() throws Exception {
        NewUser client = newClient("participa");
        String auth = bearer(client.token());
        publish(auth, G1)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("not-a-member"));

        join(auth, G1).andExpect(jsonPath("$.membership.status").value("ACTIVE"));
        String post = JsonPath.read(
                publish(auth, G1)
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id");
        mockMvc.perform(post("/posts/" + post + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\": \"¡Bienvenida!\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/posts/" + post + "/comments").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$[*].body", contains("¡Bienvenida!")));

        mockMvc.perform(put("/posts/" + post)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Editado\", \"body\": \"No es mío\"}"))
                .andExpect(status().isForbidden());
        // Juan es el creador de G1: puede borrar publicaciones de su grupo.
        mockMvc.perform(delete("/posts/" + post).header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/groups/" + G1 + "/members/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("owner-cannot-leave"));
    }

    @Test
    void reportsAreModeratedByAdmins() throws Exception {
        NewUser reporter = newClient("denuncia");
        String auth = bearer(reporter.token());
        report(auth, MARIA_POST_IN_G1).andExpect(status().isAccepted());
        report(auth, MARIA_POST_IN_G1).andExpect(status().isConflict());

        mockMvc.perform(get("/admin/moderation/reports").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isForbidden());
        String admin = bearer(token(Seed.ADMIN));
        String reports = mockMvc.perform(
                        get("/admin/moderation/reports").param("size", "50").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String reportId = ((java.util.List<String>)
                        JsonPath.read(reports, "$.content[?(@.targetId == '" + MARIA_POST_IN_G1 + "')].id"))
                .getFirst();

        mockMvc.perform(post("/admin/moderation/reports/" + reportId + "/resolve")
                        .header(HttpHeaders.AUTHORIZATION, admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"HIDE\", \"note\": \"Contenido de prueba\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.targetHidden").value(true));
        mockMvc.perform(get("/posts/" + MARIA_POST_IN_G1).header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isNotFound());
    }

    private ResultActions posts(String group, String accessToken) throws Exception {
        return mockMvc.perform(
                get("/groups/" + group + "/posts").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)));
    }

    private ResultActions join(String auth, String group) throws Exception {
        return mockMvc.perform(post("/groups/" + group + "/members").header(HttpHeaders.AUTHORIZATION, auth));
    }

    private ResultActions publish(String auth, String group) throws Exception {
        return mockMvc.perform(post("/groups/" + group + "/posts")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Hola\", \"body\": \"Me presento al grupo\"}"));
    }

    private ResultActions report(String auth, String post) throws Exception {
        return mockMvc.perform(post("/posts/" + post + "/reports")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"No es apropiado\"}"));
    }
}
