package com.habittracker.streak;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StreakCalculatorTest {

    @Test
    void noCompletions_zeroStreaks() {
        var result = StreakCalculator.calculate(Set.of(), LocalDate.of(2026, 3, 15));
        assertThat(result.currentStreak()).isZero();
        assertThat(result.longestStreak()).isZero();
    }

    @Test
    void completedToday_countsAsCurrentStreakOfOne() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var result = StreakCalculator.calculate(Set.of(today), today);
        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.longestStreak()).isEqualTo(1);
    }

    @Test
    void consecutiveRunEndingToday_isFullyCountedAsCurrent() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(
                today,
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(3)
        );
        var result = StreakCalculator.calculate(dates, today);
        assertThat(result.currentStreak()).isEqualTo(4);
        assertThat(result.longestStreak()).isEqualTo(4);
    }

    @Test
    void todayNotYetMarkedButYesterdayWas_streakStillAlive() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(3)
        );
        var result = StreakCalculator.calculate(dates, today);
        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    void lastCompletionOlderThanYesterday_currentStreakIsBroken() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(
                today.minusDays(3),
                today.minusDays(4),
                today.minusDays(5)
        );
        var result = StreakCalculator.calculate(dates, today);
        assertThat(result.currentStreak()).isZero();
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    void longestStreakCanBeInThePast_whileCurrentStreakIsShorter() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(
                today, today.minusDays(1), // current streak of 2
                today.minusDays(10), today.minusDays(11), today.minusDays(12), today.minusDays(13), today.minusDays(14) // past run of 5
        );
        var result = StreakCalculator.calculate(dates, today);
        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.longestStreak()).isEqualTo(5);
    }

    @Test
    void handlesMonthAndYearBoundaryCrossing() {
        LocalDate today = LocalDate.of(2026, 1, 1);
        var dates = Set.of(
                LocalDate.of(2025, 12, 30),
                LocalDate.of(2025, 12, 31),
                today
        );
        var result = StreakCalculator.calculate(dates, today);
        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    void nonConsecutiveDatesDoNotInflateLongestStreak() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(
                today,
                today.minusDays(5),
                today.minusDays(10)
        );
        var result = StreakCalculator.calculate(dates, today);
        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.longestStreak()).isEqualTo(1);
    }
}
