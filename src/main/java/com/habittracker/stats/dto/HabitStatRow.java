package com.habittracker.stats.dto;

public record HabitStatRow(
        Long habitId,
        String name,
        String category,
        long completedCount,
        long expectedCount,
        double completionRate
) {
}
