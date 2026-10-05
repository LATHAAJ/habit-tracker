package com.habittracker.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habittracker.ai.dto.HabitPlanResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Calls Google's Gemini API (generous free tier, no billing required) to turn a
 * free-text goal into a structured habit plan. Uses a plain HTTP call rather than
 * a client library, since there is no Gemini Java SDK already in this project's
 * dependency set and the request/response shape is small enough to construct directly.
 */
@Service
public class AiHabitPlanService {

    private static final String SYSTEM_PROMPT = """
            You are a habit-coaching assistant inside a habit tracker app. Given a goal the user \
            wants to work toward, suggest 3 to 6 concrete, trackable habits that build toward it. \
            Keep habit names short and action-oriented (e.g. "30 min focused coding practice"). \
            Each habit's category must be exactly one of: HEALTH, LEARNING, CAREER, MIND, PERSONAL, FINANCE. \
            Each habit's frequencyType is DAILY, or WEEKLY with a targetPerPeriod from 1 to 7 \
            (times per week); omit targetPerPeriod for DAILY habits.""";

    private static final Map<String, Object> RESPONSE_SCHEMA = Map.of(
            "type", "OBJECT",
            "properties", Map.of(
                    "goalSummary", Map.of("type", "STRING"),
                    "habits", Map.of(
                            "type", "ARRAY",
                            "items", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "name", Map.of("type", "STRING"),
                                            "description", Map.of("type", "STRING"),
                                            "category", Map.of(
                                                    "type", "STRING",
                                                    "enum", List.of("HEALTH", "LEARNING", "CAREER", "MIND", "PERSONAL", "FINANCE")
                                            ),
                                            "frequencyType", Map.of(
                                                    "type", "STRING",
                                                    "enum", List.of("DAILY", "WEEKLY")
                                            ),
                                            "targetPerPeriod", Map.of("type", "INTEGER")
                                    ),
                                    "required", List.of("name", "category", "frequencyType")
                            )
                    )
            ),
            "required", List.of("goalSummary", "habits")
    );

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AiHabitPlanService(
            @Value("${app.ai.gemini-api-key:}") String apiKey,
            @Value("${app.ai.gemini-model:gemini-2.0-flash-lite}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
    }

    public HabitPlanResponse generatePlan(String goal) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiUnavailableException(
                    "AI habit plan generation is not configured. Set the GEMINI_API_KEY environment variable.");
        }

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                "contents", List.of(Map.of("parts", List.of(Map.of("text", "My goal: " + goal)))),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json",
                        "responseSchema", RESPONSE_SCHEMA
                )
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model
                + ":generateContent?key=" + apiKey;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AiGenerationException("Couldn't generate a habit plan right now. Please try again.");
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("candidates").path(0).path("content").path("parts").path(0)
                    .path("text").asText(null);
            if (text == null || text.isBlank()) {
                throw new AiGenerationException("The AI didn't return a usable plan. Please try again.");
            }

            return objectMapper.readValue(text, HabitPlanResponse.class);
        } catch (AiGenerationException | AiUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiGenerationException("Couldn't generate a habit plan right now. Please try again.");
        }
    }
}
