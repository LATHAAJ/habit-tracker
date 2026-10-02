package com.habittracker.streak;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pure, side-effect-free streak math over a set of completion dates.
 * Kept separate from JPA/web so the core business logic is trivial to unit test.
 */
public class StreakCalculator {

    public record StreakResult(int currentStreak, int longestStreak) {
    }

    public static StreakResult calculate(Set<LocalDate> completedDates, LocalDate today) {
        if (completedDates.isEmpty()) {
            return new StreakResult(0, 0);
        }

        List<LocalDate> sorted = new ArrayList<>(completedDates);
        sorted.sort(LocalDate::compareTo);

        int longest = 1;
        int run = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).equals(sorted.get(i - 1).plusDays(1))) {
                run++;
            } else {
                run = 1;
            }
            longest = Math.max(longest, run);
        }

        // A streak is still "alive" through today even if today hasn't been marked yet,
        // as long as yesterday was completed — the day isn't over.
        LocalDate anchor;
        if (completedDates.contains(today)) {
            anchor = today;
        } else if (completedDates.contains(today.minusDays(1))) {
            anchor = today.minusDays(1);
        } else {
            return new StreakResult(0, longest);
        }

        int current = 1;
        LocalDate cursor = anchor.minusDays(1);
        while (completedDates.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        return new StreakResult(current, longest);
    }
}
