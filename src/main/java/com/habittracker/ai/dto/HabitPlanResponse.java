package com.habittracker.ai.dto;

import java.util.List;

public record HabitPlanResponse(
        String goalSummary,
        List<SuggestedHabit> habits
) {
}
