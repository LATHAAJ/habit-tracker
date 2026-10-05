package com.habittracker.ai.dto;

import com.habittracker.streak.Frequency;

public record SuggestedHabit(
        String name,
        String description,
        SuggestedCategory category,
        Frequency frequencyType,
        Integer targetPerPeriod
) {
}
