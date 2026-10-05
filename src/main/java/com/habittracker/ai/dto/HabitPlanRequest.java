package com.habittracker.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HabitPlanRequest(
        @NotBlank @Size(max = 300) String goal
) {
}
