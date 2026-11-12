package com.healthybites.rewards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.rewards.events.RewardEarned;
import com.healthybites.support.Seed;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.ResultActions;

/** Recompensas de punta a punta: registrar hábitos avanza la racha y completarla desbloquea el premio. */
@RecordApplicationEvents
class RewardTests extends IntegrationTest {

    private static final String CARLOS_PREMIUM_ARTICLE = "00000000-0000-7000-8000-0000000a0002";
    private static final String ELENA_GUIDE = "00000000-0000-7000-8000-0000000a0003";
    private static final String CARLOS_BASIC_PLAN = "00000000-0000-7000-8000-0000000d0003";

    @Autowired
    private ApplicationEvents events;

    @Test
    void loggingHabitsCompletesTheStreakAndUnlocksThePrize() throws Exception {
        String reward = createReward(bearer(token(Seed.CARLOS)), 2, CARLOS_PREMIUM_ARTICLE, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.target.type").value("CONTENT"))
                .andExpect(jsonPath("$.target.title").value("Cómo interpretar tu composición corporal"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String rewardId = JsonPath.read(reward, "$.id");

        NewUser client = newClient("racha");
        String auth = bearer(client.token());
        mockMvc.perform(get("/content-items/como-interpretar-tu-composicion-corporal")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isForbidden());
        startStreak(auth, rewardId)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.streak.currentDays").value(0));
        startStreak(auth, rewardId).andExpect(status().isConflict());

        logHabits(auth, LocalDate.now().minusDays(1));
        mockMvc.perform(get("/me/streaks").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$[0].streak.currentDays").value(1));
        logHabits(auth, LocalDate.now());

        mockMvc.perform(get("/me/streaks").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$[0].streak.completed").value(true));
        mockMvc.perform(get("/content-items/como-interpretar-tu-composicion-corporal")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk());
        mockMvc.perform(get("/me/content-unlocks").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$[*].slug", hasItem("como-interpretar-tu-composicion-corporal")));
        assertThat(events.stream(RewardEarned.class)).anyMatch(e -> e.clientId().equals(client.id()));
    }

    @Test
    void thePrizeMustBeTheNutritionistsOwnPublishedContentOrPlanAndOnlyOne() throws Exception {
        String carlos = bearer(token(Seed.CARLOS));
        createReward(carlos, 7, ELENA_GUIDE, null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("invalid-reward-target"));
        createReward(carlos, 7, CARLOS_PREMIUM_ARTICLE, CARLOS_BASIC_PLAN).andExpect(status().isUnprocessableContent());
        createReward(carlos, 7, null, null).andExpect(status().isUnprocessableContent());
        createReward(carlos, 7, null, CARLOS_BASIC_PLAN)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.target.type").value("MEAL_PLAN"));
    }

    @Test
    void clientsSeeActiveRewardsWithTheirProgressAndDeactivatedOnesDisappear() throws Exception {
        mockMvc.perform(get("/rewards").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.title == 'Constancia de 7 días')].streak.currentDays", hasItem(2)));

        String carlos = bearer(token(Seed.CARLOS));
        String id = JsonPath.read(
                createReward(carlos, 3, null, CARLOS_BASIC_PLAN)
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id");
        mockMvc.perform(delete("/nutritionist/rewards/" + id).header(HttpHeaders.AUTHORIZATION, carlos))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/rewards").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(jsonPath("$.content[*].id", org.hamcrest.Matchers.not(hasItem(id))));
        // El nutricionista filtra sus recompensas retiradas.
        mockMvc.perform(get("/nutritionist/rewards").param("active", "false").header(HttpHeaders.AUTHORIZATION, carlos))
                .andExpect(jsonPath("$.content[*].id", hasItem(id)))
                .andExpect(jsonPath(
                        "$.content[*].active", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(false))));
        mockMvc.perform(delete("/nutritionist/rewards/" + id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ELENA))))
                .andExpect(status().isNotFound());
    }

    private ResultActions createReward(String auth, int days, String content, String plan) throws Exception {
        return mockMvc.perform(post("/nutritionist/rewards")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Premio de prueba", "description": "Días seguidos", "requiredStreakDays": %d,
                         "contentItemId": %s, "mealPlanId": %s}
                        """.formatted(days, quote(content), quote(plan))));
    }

    private ResultActions startStreak(String auth, String rewardId) throws Exception {
        return mockMvc.perform(post("/me/streaks")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rewardId\": \"" + rewardId + "\"}"));
    }

    private void logHabits(String auth, LocalDate day) throws Exception {
        mockMvc.perform(post("/me/habit-logs")
                        .header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loggedOn": "%s", "hydration": 8, "nutrition": 8, "exercise": 8, "sleep": 8}
                                """.formatted(day)))
                .andExpect(status().isCreated());
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
