package com.habittracker.habit.dto;

import com.habittracker.streak.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HabitRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        Boolean active,
        @Size(max = 60) String category,
        Frequency frequencyType,
        Integer targetPerPeriod
) {
}
