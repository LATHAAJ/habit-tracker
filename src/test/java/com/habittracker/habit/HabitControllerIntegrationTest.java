package com.habittracker.habit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.auth.dto.AuthResponse;
import com.habittracker.auth.dto.LoginRequest;
import com.habittracker.auth.dto.SignupRequest;
import com.habittracker.habit.dto.HabitRequest;
import com.habittracker.habit.dto.HabitResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HabitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String signup(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(email, "password123"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class).token();
    }

    private HabitResponse readHabit(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), HabitResponse.class);
    }

    @Test
    void unauthenticatedRequestToHabitsIsRejected() throws Exception {
        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/habits").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signupLoginCreateHabitToggleAndStreakReflectsInList() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String token = signup(email);

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HabitRequest("Drink water", "8 glasses a day", null))))
                .andExpect(status().isCreated())
                .andReturn();
        HabitResponse created = readHabit(createResult);
        assertThat(created.currentStreak()).isZero();
        assertThat(created.completedToday()).isFalse();

        MvcResult toggleResult = mockMvc.perform(post("/api/habits/" + created.id() + "/toggle")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        HabitResponse toggled = readHabit(toggleResult);
        assertThat(toggled.completedToday()).isTrue();
        assertThat(toggled.currentStreak()).isEqualTo(1);

        MvcResult listResult = mockMvc.perform(get("/api/habits").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        HabitResponse[] list = objectMapper.readValue(listResult.getResponse().getContentAsString(), HabitResponse[].class);
        assertThat(list).hasSize(1);
        assertThat(list[0].currentStreak()).isEqualTo(1);

        // toggling again un-marks today (idempotent toggle)
        MvcResult untoggleResult = mockMvc.perform(post("/api/habits/" + created.id() + "/toggle")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        HabitResponse untoggled = readHabit(untoggleResult);
        assertThat(untoggled.completedToday()).isFalse();
        assertThat(untoggled.currentStreak()).isZero();
    }

    @Test
    void usersCannotSeeOrModifyEachOthersHabits() throws Exception {
        String tokenA = signup("user-a-" + UUID.randomUUID() + "@example.com");
        String tokenB = signup("user-b-" + UUID.randomUUID() + "@example.com");

        MvcResult createResult = mockMvc.perform(post("/api/habits")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HabitRequest("A's secret habit", null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        Long habitId = readHabit(createResult).id();

        MvcResult listAsB = mockMvc.perform(get("/api/habits").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andReturn();
        HabitResponse[] listBody = objectMapper.readValue(listAsB.getResponse().getContentAsString(), HabitResponse[].class);
        assertThat(listBody).isEmpty();

        mockMvc.perform(put("/api/habits/" + habitId)
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HabitRequest("hijacked", null, null))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/habits/" + habitId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateSignupEmailIsRejected() throws Exception {
        String email = "dup-" + UUID.randomUUID() + "@example.com";
        signup(email);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest(email, "password123"))))
                .andExpect(status().isConflict());
    }

    @Test
    void wrongPasswordOnLoginIsRejected() throws Exception {
        String email = "login-" + UUID.randomUUID() + "@example.com";
        signup(email);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }
}
