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

    @Test
    void calculate_delegatesToDailyWhenFrequencyIsDaily() {
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(today, today.minusDays(1), today.minusDays(2));
        var withFrequency = StreakCalculator.calculate(dates, today, Frequency.DAILY, 1);
        var plain = StreakCalculator.calculate(dates, today);
        assertThat(withFrequency).isEqualTo(plain);
    }

    @Test
    void weeklyTarget_emptyCompletions_zeroStreaks() {
        LocalDate today = LocalDate.of(2026, 3, 15); // a Sunday
        var result = StreakCalculator.calculate(Set.of(), today, Frequency.WEEKLY, 3);
        assertThat(result.currentStreak()).isZero();
        assertThat(result.longestStreak()).isZero();
    }

    @Test
    void weeklyTarget_metEveryWeek_currentAndLongestMatchWeekCount() {
        // Mondays: 2026-02-23, 2026-03-02, 2026-03-09; today in the third week (Sunday 2026-03-15)
        LocalDate today = LocalDate.of(2026, 3, 15);
        var dates = Set.of(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 2, 24), LocalDate.of(2026, 2, 25), // week 1: 3x
                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 4),     // week 2: 3x
                LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11)    // week 3: 3x
        );
        var result = StreakCalculator.calculate(dates, today, Frequency.WEEKLY, 3);
        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.longestStreak()).isEqualTo(3);
    }

    @Test
    void weeklyTarget_currentWeekInProgressButLastWeekMet_streakStillAlive() {
        // Last week (Mon 2026-03-09) hit target of 3; this week (Mon 2026-03-16) has only 1 so far,
        // and today (Wed 2026-03-18) is still within this week, so the streak isn't broken yet.
        LocalDate today = LocalDate.of(2026, 3, 18);
        var dates = Set.of(
                LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11),
                LocalDate.of(2026, 3, 16)
        );
        var result = StreakCalculator.calculate(dates, today, Frequency.WEEKLY, 3);
        assertThat(result.currentStreak()).isEqualTo(1);
    }

    @Test
    void weeklyTarget_missedWeekBreaksStreak_longestStreakPreservedInPast() {
        // Weeks of 2026-02-23 and 2026-03-02 both hit target of 2, then a gap week, then today's
        // week only has 1 completion (not yet meeting target) and last week had none either.
        LocalDate today = LocalDate.of(2026, 3, 18);
        var dates = Set.of(
                LocalDate.of(2026, 2, 23), LocalDate.of(2026, 2, 24),
                LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 3),
                LocalDate.of(2026, 3, 17)
        );
        var result = StreakCalculator.calculate(dates, today, Frequency.WEEKLY, 2);
        assertThat(result.currentStreak()).isZero();
        assertThat(result.longestStreak()).isEqualTo(2);
    }
}
