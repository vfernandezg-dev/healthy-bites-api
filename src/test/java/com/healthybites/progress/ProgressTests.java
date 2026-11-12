package com.healthybites.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.progress.events.GoalCompleted;
import com.healthybites.progress.events.HabitLogged;
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

/** Metas, pesajes, hábitos y recomendación: solo lo propio y con las reglas del dominio. */
@RecordApplicationEvents
class ProgressTests extends IntegrationTest {

    private static final String JUAN_ACTIVE_GOAL = "00000000-0000-7000-8000-000000001001";

    @Autowired
    private ApplicationEvents events;

    /** Un listado pide como mucho un año: nunca toda la historia de golpe. */
    @Test
    void historiesAreRequestedAYearAtATime() throws Exception {
        String auth = bearer(token(Seed.JUAN));
        mockMvc.perform(get("/me/habit-logs")
                        .param("from", "2020-01-01")
                        .param("to", "2026-01-01")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("range-too-large"))
                .andExpect(jsonPath("$.maxDays").value(366));
        mockMvc.perform(get("/me/habit-logs")
                        .param("from", "2026-02-01")
                        .param("to", "2026-01-01")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("invalid-range"));
    }

    @Test
    void aClientHasOneActiveGoalAndItsHistory() throws Exception {
        NewUser client = newClient("metas");
        String auth = bearer(client.token());
        String id = JsonPath.read(
                createGoal(auth, "70.0")
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.status").value("ACTIVE"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id");
        createGoal(auth, "65.0")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("active-goal-exists"));

        mockMvc.perform(post("/me/goals/" + id + "/complete").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        mockMvc.perform(get("/me/goals/" + id + "/status-history").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$[*].to", contains("ACTIVE", "COMPLETED")));
        mockMvc.perform(post("/me/goals/" + id + "/weight-entries")
                        .header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(weightBody(LocalDate.now().toString(), "70.0")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("goal-closed"));
        assertThat(events.stream(GoalCompleted.class))
                .anyMatch(e -> e.clientId().equals(client.id()));

        createGoal(auth, "65.0").andExpect(status().isCreated());
    }

    @Test
    void weightEntriesAreOnePerDayNotInTheFutureAndOnlyOnYourOwnGoals() throws Exception {
        NewUser client = newClient("pesajes");
        String auth = bearer(client.token());
        String goal =
                JsonPath.read(createGoal(auth, "70.0").andReturn().getResponse().getContentAsString(), "$.id");
        String today = LocalDate.now().toString();

        addWeight(auth, goal, LocalDate.now().minusDays(7).toString(), "73.5").andExpect(status().isCreated());
        addWeight(auth, goal, today, "73.1").andExpect(status().isCreated());
        addWeight(auth, goal, today, "73.0").andExpect(status().isConflict());
        addWeight(auth, goal, LocalDate.now().plusDays(1).toString(), "73.0")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("future-date"));

        mockMvc.perform(get("/me/goals/" + goal + "/weight-entries")
                        .param("from", LocalDate.now().minusDays(1).toString())
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$[*].weightKg", contains(73.1)));

        // La meta de Juan no existe para otro cliente.
        mockMvc.perform(get("/me/goals/" + JUAN_ACTIVE_GOAL + "/weight-entries")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isNotFound());
    }

    @Test
    void habitsAreLoggedForTodayOrYesterdayOncePerDay() throws Exception {
        NewUser client = newClient("habitos");
        String auth = bearer(client.token());

        logHabits(auth, null, 8)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.average").value(8.0));
        logHabits(auth, null, 7)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("habit-log-exists"));
        logHabits(auth, LocalDate.now().minusDays(1).toString(), 6).andExpect(status().isCreated());
        logHabits(auth, LocalDate.now().minusDays(3).toString(), 6)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("log-date-out-of-range"));
        logHabits(auth, null, 11).andExpect(status().isUnprocessableContent());

        mockMvc.perform(get("/me/habit-logs").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.length()").value(2));
        assertThat(events.stream(HabitLogged.class).filter(e -> e.clientId().equals(client.id())))
                .hasSize(2);
    }

    @Test
    void theRecommendationUsesTheProfileHeightAndTheLatestWeight() throws Exception {
        // Juan: 175 cm, último pesaje 72.0 kg, objetivo 68 kg.
        mockMvc.perform(get("/me/goals/" + JUAN_ACTIVE_GOAL + "/recommendation")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthyMinKg").value(56.7))
                .andExpect(jsonPath("$.healthyMaxKg").value(76.3))
                .andExpect(jsonPath("$.currentWeightKg").value(72.0))
                .andExpect(jsonPath("$.targetInHealthyRange").value(true))
                .andExpect(jsonPath("$.estimatedWeeks").value(8));

        NewUser noHeight = newClient("sin-altura");
        String goal = JsonPath.read(
                createGoal(bearer(noHeight.token()), "70.0")
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.id");
        mockMvc.perform(get("/me/goals/" + goal + "/recommendation")
                        .header(HttpHeaders.AUTHORIZATION, bearer(noHeight.token())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("profile-incomplete"));
    }

    @Test
    void progressIsOnlyForClients() throws Exception {
        mockMvc.perform(get("/me/goals").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.CARLOS))))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/me/goals/" + JUAN_ACTIVE_GOAL)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Ajena\", \"targetWeightKg\": 60}"))
                .andExpect(status().isNotFound());
    }

    private ResultActions createGoal(String auth, String target) throws Exception {
        return mockMvc.perform(post("/me/goals")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Mi meta\", \"targetWeightKg\": " + target + "}"));
    }

    private ResultActions addWeight(String auth, String goal, String day, String kg) throws Exception {
        return mockMvc.perform(post("/me/goals/" + goal + "/weight-entries")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(weightBody(day, kg)));
    }

    private static String weightBody(String day, String kg) {
        return "{\"measuredOn\": \"" + day + "\", \"weightKg\": " + kg + "}";
    }

    private ResultActions logHabits(String auth, String day, int score) throws Exception {
        String date = day == null ? "null" : "\"" + day + "\"";
        return mockMvc.perform(post("/me/habit-logs")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"loggedOn": %s, "hydration": %d, "nutrition": %d, "exercise": %d, "sleep": %d}
                        """.formatted(date, score, score, score, score)));
    }
}
