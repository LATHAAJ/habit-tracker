package com.habittracker.streak;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Frequency-aware streak calculation. DAILY delegates to the consecutive-day
     * logic above; WEEKLY buckets completions by calendar week and runs the same
     * consecutive-run logic over weeks that met the target count.
     */
    public static StreakResult calculate(Set<LocalDate> completedDates, LocalDate today,
                                          Frequency frequency, int targetPerPeriod) {
        if (frequency == Frequency.DAILY) {
            return calculate(completedDates, today);
        }
        return calculateWeekly(completedDates, today, targetPerPeriod);
    }

    private static LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static StreakResult calculateWeekly(Set<LocalDate> completedDates, LocalDate today, int targetPerPeriod) {
        if (completedDates.isEmpty()) {
            return new StreakResult(0, 0);
        }

        Map<LocalDate, Long> countsByWeek = completedDates.stream()
                .collect(Collectors.groupingBy(StreakCalculator::weekStart, Collectors.counting()));

        LocalDate firstWeek = countsByWeek.keySet().stream().min(LocalDate::compareTo).orElseThrow();
        LocalDate currentWeek = weekStart(today);

        List<Boolean> met = new ArrayList<>();
        for (LocalDate week = firstWeek; !week.isAfter(currentWeek); week = week.plusWeeks(1)) {
            met.add(countsByWeek.getOrDefault(week, 0L) >= targetPerPeriod);
        }

        int longest = 0;
        int run = 0;
        for (boolean weekMet : met) {
            run = weekMet ? run + 1 : 0;
            longest = Math.max(longest, run);
        }

        int lastIndex = met.size() - 1;
        int current;
        if (met.get(lastIndex)) {
            current = trailingRun(met, lastIndex);
        } else if (lastIndex >= 1 && met.get(lastIndex - 1)) {
            // Current week hasn't met target yet, but it isn't over — streak still alive
            // from last week, same idea as "today not marked but yesterday was" above.
            current = trailingRun(met, lastIndex - 1);
        } else {
            current = 0;
        }

        return new StreakResult(current, longest);
    }

    private static int trailingRun(List<Boolean> met, int fromIndexInclusive) {
        int run = 0;
        for (int i = fromIndexInclusive; i >= 0 && met.get(i); i--) {
            run++;
        }
        return run;
    }
}
