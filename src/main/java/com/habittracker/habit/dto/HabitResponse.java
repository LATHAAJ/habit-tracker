package com.habittracker.habit.dto;

import java.time.Instant;

public record HabitResponse(
        Long id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        int currentStreak,
        int longestStreak,
        boolean completedToday
) {
}
