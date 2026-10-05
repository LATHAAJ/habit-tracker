package com.habittracker.habit.dto;

import com.habittracker.streak.Frequency;

import java.time.Instant;

public record HabitResponse(
        Long id,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        int currentStreak,
        int longestStreak,
        boolean completedToday,
        String category,
        Frequency frequencyType,
        int targetPerPeriod
) {
}
