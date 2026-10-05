package com.habittracker.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.auth.dto.AuthResponse;
import com.habittracker.auth.dto.SignupRequest;
import com.habittracker.habit.dto.HabitRequest;
import com.habittracker.habit.dto.HabitResponse;
import com.habittracker.stats.dto.HabitStatRow;
import com.habittracker.stats.dto.TrendPointResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String signup(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(email, "password123", "Test User"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class).token();
    }

    private Long createHabit(String token, String name, String category, com.habittracker.streak.Frequency frequency, Integer target) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HabitRequest(name, null, null, category, frequency, target))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), HabitResponse.class).id();
    }

    private void toggle(String token, Long habitId, LocalDate date) throws Exception {
        mockMvc.perform(post("/api/habits/" + habitId + "/toggle?date=" + date)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void trendEndpoint_returnsOrderedPeriodsWithCompletionRates() throws Exception {
        String token = signup("trend-" + UUID.randomUUID() + "@example.com");
        Long habitId = createHabit(token, "Meditate", null, null, null);
        toggle(token, habitId, LocalDate.now());

        MvcResult result = mockMvc.perform(get("/api/stats/trend?granularity=WEEK&periods=4")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        TrendPointResponse[] points = objectMapper.readValue(result.getResponse().getContentAsString(), TrendPointResponse[].class);

        assertThat(points).hasSize(4);
        for (int i = 1; i < points.length; i++) {
            assertThat(points[i].periodStart()).isAfter(points[i - 1].periodStart());
        }
        TrendPointResponse lastPoint = points[points.length - 1];
        assertThat(lastPoint.completedCount()).isEqualTo(1);
        assertThat(lastPoint.completionRate()).isGreaterThan(0.0);
    }

    @Test
    void habitsRankingEndpoint_sortsByCompletionRateDescending() throws Exception {
        String token = signup("rank-" + UUID.randomUUID() + "@example.com");
        Long dailyDone = createHabit(token, "Done daily", null, null, null);
        Long dailyMissed = createHabit(token, "Missed daily", null, null, null);
        toggle(token, dailyDone, LocalDate.now());

        LocalDate from = LocalDate.now().minusDays(6);
        LocalDate to = LocalDate.now();
        MvcResult result = mockMvc.perform(get("/api/stats/habits?from=" + from + "&to=" + to)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        HabitStatRow[] rows = objectMapper.readValue(result.getResponse().getContentAsString(), HabitStatRow[].class);

        assertThat(rows).hasSize(2);
        assertThat(rows[0].habitId()).isEqualTo(dailyDone);
        assertThat(rows[0].completionRate()).isGreaterThan(rows[1].completionRate());
    }

    @Test
    void invalidPeriodsParam_isRejected() throws Exception {
        String token = signup("invalid-periods-" + UUID.randomUUID() + "@example.com");
        mockMvc.perform(get("/api/stats/trend?granularity=WEEK&periods=0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/stats/trend?granularity=WEEK&periods=100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statsAreScopedToCallingUser_otherUsersHabitsExcluded() throws Exception {
        String tokenA = signup("stats-a-" + UUID.randomUUID() + "@example.com");
        String tokenB = signup("stats-b-" + UUID.randomUUID() + "@example.com");
        createHabit(tokenA, "A's habit", null, null, null);

        LocalDate from = LocalDate.now().minusDays(6);
        LocalDate to = LocalDate.now();
        MvcResult result = mockMvc.perform(get("/api/stats/habits?from=" + from + "&to=" + to)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();
        HabitStatRow[] rows = objectMapper.readValue(result.getResponse().getContentAsString(), HabitStatRow[].class);
        assertThat(rows).isEmpty();
    }
}
