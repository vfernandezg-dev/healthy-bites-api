package com.healthybites.nutrition;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Locale;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Planes de alimentación: acceso por nivel o recompensa decidido en el servidor, inscripciones y el espacio del
 * nutricionista (solo sobre lo suyo).
 */
class MealPlanTests extends IntegrationTest {

    // Seed: P1 PREMIUM y P3 BASIC de Carlos; P2 VIP y P4 BASIC de Elena; P5 borrador de Carlos.
    private static final String P1 = "00000000-0000-7000-8000-0000000d0001";
    private static final String P2 = "00000000-0000-7000-8000-0000000d0002";
    private static final String P3 = "00000000-0000-7000-8000-0000000d0003";
    private static final String P5 = "00000000-0000-7000-8000-0000000d0005";

    @Autowired
    private MealPlanAccess mealPlanAccess;

    /** El resumen por estados coincide con la lista filtrada: el panel no necesita descargar la lista entera. */
    @Test
    void theStatusSummaryMatchesTheFilteredList() throws Exception {
        String auth = bearer(token(Seed.CARLOS));
        String summary = mockMvc.perform(
                        get("/nutritionist/meal-plans/summary").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        for (String state : List.of("DRAFT", "PUBLISHED", "ARCHIVED")) {
            int counted = JsonPath.read(summary, "$." + state.toLowerCase(Locale.ROOT));
            mockMvc.perform(get("/nutritionist/meal-plans")
                            .param("status", state)
                            .param("size", "100")
                            .header(HttpHeaders.AUTHORIZATION, auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value(counted))
                    .andExpect(jsonPath("$.content[*].status", Matchers.everyItem(Matchers.is(state))));
        }
        int published = JsonPath.read(summary, "$.published");
        org.assertj.core.api.Assertions.assertThat(published).isPositive();
    }

    @Test
    void theCatalogMarksWhatEachClientCanOpenAndHidesDrafts() throws Exception {
        mockMvc.perform(get("/meal-plans")
                        .param("size", "50")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '" + P1 + "')].locked", contains(true)))
                .andExpect(jsonPath("$.content[?(@.id == '" + P3 + "')].locked", contains(false)))
                .andExpect(jsonPath("$.content[?(@.id == '" + P1 + "')].author.name", contains("Carlos García")))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(P5))))
                .andExpect(jsonPath("$.content[0].averageDailyCalories").isNumber());
    }

    @Test
    void lockedPlansAre403WithTheRequiredTier() throws Exception {
        detail(P1, Seed.JUAN)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("subscription-required"))
                .andExpect(jsonPath("$.requiredTier").value("PREMIUM"));
        detail(P1, Seed.MARIA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meals.length()").value(4));
        detail(P2, Seed.MARIA)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.requiredTier").value("VIP"));
        detail(P2, Seed.PEDRO).andExpect(status().isOk());
        detail(P5, Seed.JUAN).andExpect(status().isNotFound());
    }

    @Test
    void clientsFollowPlansTheyCanOpenAndRewardsUnlockThem() throws Exception {
        NewUser client = newClient("planes");
        enroll(client.token(), P1).andExpect(status().isForbidden());
        enroll(client.token(), P3).andExpect(status().isCreated());
        enroll(client.token(), P3).andExpect(status().isCreated()); // repetir no duplica

        mockMvc.perform(get("/me/meal-plan-enrollments").header(HttpHeaders.AUTHORIZATION, bearer(client.token())))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].source").value("SELF"));

        // Recompensa: P1 queda desbloqueado aunque su nivel sea BASIC, y no se puede quitar.
        mealPlanAccess.grant(java.util.UUID.fromString(P1), client.id());
        detail(P1, client.token(), true).andExpect(status().isOk());
        mockMvc.perform(delete("/me/meal-plan-enrollments/" + P1)
                        .header(HttpHeaders.AUTHORIZATION, bearer(client.token())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("reward-enrollment"));
        mockMvc.perform(delete("/me/meal-plan-enrollments/" + P3)
                        .header(HttpHeaders.AUTHORIZATION, bearer(client.token())))
                .andExpect(status().isNoContent());
    }

    @Test
    void nutritionistsBuildPublishAndArchiveTheirOwnPlans() throws Exception {
        String elena = bearer(token(Seed.ELENA));
        String created = mockMvc.perform(post("/nutritionist/meal-plans")
                        .header(HttpHeaders.AUTHORIZATION, elena)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Plan exprés", "description": "Tres días de prueba.", "goal": "MAINTENANCE",
                                 "durationDays": 3, "requiredTier": "BASIC"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = JsonPath.read(created, "$.plan.id");

        mockMvc.perform(post("/nutritionist/meal-plans/" + id + "/publish").header(HttpHeaders.AUTHORIZATION, elena))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("plan-without-meals"));
        addMeal(elena, id, 4, 0)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("day-outside-plan"));
        addMeal(elena, id, 1, 0)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(400));
        addMeal(elena, id, 1, 0)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("meal-slot-taken"));

        // Lo de otro nutricionista no existe para Carlos.
        mockMvc.perform(put("/nutritionist/meal-plans/" + id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.CARLOS)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Robado", "description": "x", "goal": "MAINTENANCE", "durationDays": 3,
                                 "requiredTier": "BASIC"}"""))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/nutritionist/meal-plans/" + id + "/publish").header(HttpHeaders.AUTHORIZATION, elena))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        detail(id, Seed.JUAN).andExpect(status().isOk());
        mockMvc.perform(delete("/nutritionist/meal-plans/" + id).header(HttpHeaders.AUTHORIZATION, elena))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/nutritionist/meal-plans/" + id + "/archive").header(HttpHeaders.AUTHORIZATION, elena))
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
        detail(id, Seed.JUAN).andExpect(status().isNotFound());
    }

    @Test
    void nutritionistsSeeTheClientsOfTheirPlansAndClientsCannotUseTheirSpace() throws Exception {
        mockMvc.perform(get("/nutritionist/clients").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.CARLOS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(Seed.JUAN)))
                .andExpect(jsonPath(
                        "$[?(@.email == '" + Seed.JUAN + "')].plans[0]", contains("Mantenimiento equilibrado")));
        mockMvc.perform(get("/nutritionist/meal-plans").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isForbidden());
    }

    private ResultActions detail(String planId, String email) throws Exception {
        return detail(planId, token(email), true);
    }

    private ResultActions detail(String planId, String accessToken, boolean ignored) throws Exception {
        return mockMvc.perform(get("/meal-plans/" + planId).header(HttpHeaders.AUTHORIZATION, bearer(accessToken)));
    }

    private ResultActions enroll(String accessToken, String planId) throws Exception {
        return mockMvc.perform(post("/me/meal-plan-enrollments")
                .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mealPlanId\": \"" + planId + "\"}"));
    }

    private ResultActions addMeal(String auth, String planId, int day, int position) throws Exception {
        return mockMvc.perform(post("/nutritionist/meal-plans/" + planId + "/meals")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"dayNumber": %d, "position": %d, "category": "LUNCH", "name": "Ensalada completa",
                         "calories": 400}""".formatted(day, position)));
    }
}
