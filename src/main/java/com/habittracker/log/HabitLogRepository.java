package com.habittracker.log;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByHabitId(Long habitId);
    List<HabitLog> findByHabitIdAndDateBetween(Long habitId, LocalDate from, LocalDate to);
    List<HabitLog> findByHabit_Owner_IdAndDateBetween(Long ownerId, LocalDate from, LocalDate to);
    Optional<HabitLog> findByHabitIdAndDate(Long habitId, LocalDate date);
    void deleteByHabitId(Long habitId);
}
