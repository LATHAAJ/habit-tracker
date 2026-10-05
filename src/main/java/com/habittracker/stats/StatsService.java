package com.habittracker.stats;

import com.habittracker.habit.Habit;
import com.habittracker.habit.HabitRepository;
import com.habittracker.log.HabitLog;
import com.habittracker.log.HabitLogRepository;
import com.habittracker.stats.dto.HabitStatRow;
import com.habittracker.stats.dto.TrendPointResponse;
import com.habittracker.streak.Frequency;
import com.habittracker.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.time.DayOfWeek.MONDAY;

@Service
public class StatsService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    public StatsService(HabitRepository habitRepository, HabitLogRepository habitLogRepository) {
        this.habitRepository = habitRepository;
        this.habitLogRepository = habitLogRepository;
    }

    @Transactional(readOnly = true)
    public List<TrendPointResponse> trend(User owner, Granularity granularity, int periods) {
        if (periods < 1 || periods > 52) {
            throw new InvalidStatsRangeException("periods must be between 1 and 52");
        }

        LocalDate today = LocalDate.now();
        List<LocalDate[]> buckets = new ArrayList<>();
        if (granularity == Granularity.WEEK) {
            LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(MONDAY));
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate start = currentWeekStart.minusWeeks(i);
                LocalDate end = minDate(start.plusDays(6), today);
                buckets.add(new LocalDate[]{start, end});
            }
        } else {
            LocalDate currentMonthStart = today.withDayOfMonth(1);
            for (int i = periods - 1; i >= 0; i--) {
                LocalDate start = currentMonthStart.minusMonths(i);
                LocalDate end = minDate(start.plusMonths(1).minusDays(1), today);
                buckets.add(new LocalDate[]{start, end});
            }
        }

        LocalDate overallFrom = buckets.get(0)[0];
        List<HabitLog> logs = habitLogRepository.findByHabit_Owner_IdAndDateBetween(owner.getId(), overallFrom, today);
        long activeHabitCount = habitRepository.findByOwnerIdOrderByCreatedAtAsc(owner.getId()).stream()
                .filter(Habit::isActive)
                .count();

        List<TrendPointResponse> points = new ArrayList<>();
        for (LocalDate[] bucket : buckets) {
            LocalDate start = bucket[0];
            LocalDate end = bucket[1];
            long completedCount = logs.stream()
                    .filter(log -> !log.getDate().isBefore(start) && !log.getDate().isAfter(end))
                    .count();
            long daysInPeriod = ChronoUnit.DAYS.between(start, end) + 1;
            long possibleCount = activeHabitCount * daysInPeriod;
            double completionRate = possibleCount == 0 ? 0.0 : completedCount / (double) possibleCount;
            points.add(new TrendPointResponse(start, end, completedCount, possibleCount, completionRate));
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<HabitStatRow> rankHabits(User owner, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidStatsRangeException("from must not be after to");
        }

        long daysInRange = ChronoUnit.DAYS.between(from, to) + 1;
        List<Habit> habits = habitRepository.findByOwnerIdOrderByCreatedAtAsc(owner.getId());

        List<HabitStatRow> rows = new ArrayList<>();
        for (Habit habit : habits) {
            long completedCount = habitLogRepository.findByHabitIdAndDateBetween(habit.getId(), from, to).size();
            Frequency frequency = habit.getFrequencyType() != null ? habit.getFrequencyType() : Frequency.DAILY;
            int target = habit.getTargetPerPeriod() != null ? habit.getTargetPerPeriod() : 1;
            long expectedCount = frequency == Frequency.DAILY
                    ? daysInRange
                    : (long) Math.ceil(daysInRange / 7.0) * target;
            double completionRate = expectedCount == 0 ? 0.0 : Math.min(1.0, completedCount / (double) expectedCount);
            rows.add(new HabitStatRow(habit.getId(), habit.getName(), habit.getCategory(), completedCount, expectedCount, completionRate));
        }

        rows.sort(Comparator.comparingDouble(HabitStatRow::completionRate).reversed());
        return rows;
    }

    private static LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
