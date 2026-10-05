package com.habittracker.habit;

import com.habittracker.habit.dto.HabitRequest;
import com.habittracker.habit.dto.HabitResponse;
import com.habittracker.log.HabitLog;
import com.habittracker.log.HabitLogRepository;
import com.habittracker.streak.Frequency;
import com.habittracker.streak.StreakCalculator;
import com.habittracker.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;

    public HabitService(HabitRepository habitRepository, HabitLogRepository habitLogRepository) {
        this.habitRepository = habitRepository;
        this.habitLogRepository = habitLogRepository;
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> listHabits(User owner, String category) {
        List<Habit> habits = (category == null || category.isBlank())
                ? habitRepository.findByOwnerIdOrderByCreatedAtAsc(owner.getId())
                : habitRepository.findByOwnerIdAndCategoryOrderByCreatedAtAsc(owner.getId(), category);
        return habits.stream().map(this::toResponse).toList();
    }

    @Transactional
    public HabitResponse createHabit(User owner, HabitRequest request) {
        Habit habit = new Habit(request.name().trim(), request.description(), owner);
        if (request.active() != null) {
            habit.setActive(request.active());
        }
        applyFrequency(habit, request);
        habitRepository.save(habit);
        return toResponse(habit);
    }

    @Transactional
    public HabitResponse updateHabit(User owner, Long habitId, HabitRequest request) {
        Habit habit = getOwnedHabit(owner, habitId);
        habit.setName(request.name().trim());
        habit.setDescription(request.description());
        if (request.active() != null) {
            habit.setActive(request.active());
        }
        applyFrequency(habit, request);
        habitRepository.save(habit);
        return toResponse(habit);
    }

    @Transactional
    public void deleteHabit(User owner, Long habitId) {
        Habit habit = getOwnedHabit(owner, habitId);
        habitLogRepository.deleteByHabitId(habit.getId());
        habitRepository.delete(habit);
    }

    @Transactional
    public HabitResponse toggleCompletion(User owner, Long habitId, LocalDate date) {
        Habit habit = getOwnedHabit(owner, habitId);
        habitLogRepository.findByHabitIdAndDate(habit.getId(), date)
                .ifPresentOrElse(
                        habitLogRepository::delete,
                        () -> habitLogRepository.save(new HabitLog(habit, date))
                );
        return toResponse(habit);
    }

    @Transactional(readOnly = true)
    public List<String> getCompletedDates(User owner, Long habitId, LocalDate from, LocalDate to) {
        Habit habit = getOwnedHabit(owner, habitId);
        return habitLogRepository.findByHabitIdAndDateBetween(habit.getId(), from, to).stream()
                .map(log -> log.getDate().toString())
                .sorted()
                .toList();
    }

    private void applyFrequency(Habit habit, HabitRequest request) {
        Frequency frequency = request.frequencyType() != null ? request.frequencyType() : Frequency.DAILY;
        if (frequency == Frequency.WEEKLY) {
            int target = request.targetPerPeriod() != null ? request.targetPerPeriod() : 0;
            if (target < 1 || target > 7) {
                throw new InvalidFrequencyException("targetPerPeriod must be between 1 and 7 for WEEKLY habits");
            }
            habit.setTargetPerPeriod(target);
        } else {
            habit.setTargetPerPeriod(null);
        }
        habit.setFrequencyType(frequency);
        habit.setCategory(request.category() != null ? request.category().trim() : null);
    }

    private Habit getOwnedHabit(User owner, Long habitId) {
        return habitRepository.findByIdAndOwnerId(habitId, owner.getId())
                .orElseThrow(() -> new HabitNotFoundException(habitId));
    }

    private HabitResponse toResponse(Habit habit) {
        Set<LocalDate> completedDates = habitLogRepository.findByHabitId(habit.getId()).stream()
                .map(HabitLog::getDate)
                .collect(Collectors.toSet());
        LocalDate today = LocalDate.now();
        Frequency frequency = habit.getFrequencyType() != null ? habit.getFrequencyType() : Frequency.DAILY;
        int target = habit.getTargetPerPeriod() != null ? habit.getTargetPerPeriod() : 1;
        StreakCalculator.StreakResult streak = StreakCalculator.calculate(completedDates, today, frequency, target);
        return new HabitResponse(
                habit.getId(),
                habit.getName(),
                habit.getDescription(),
                habit.isActive(),
                habit.getCreatedAt(),
                streak.currentStreak(),
                streak.longestStreak(),
                completedDates.contains(today),
                habit.getCategory(),
                frequency,
                target
        );
    }
}
