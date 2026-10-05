package com.habittracker.habit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByOwnerIdOrderByCreatedAtAsc(Long ownerId);
    List<Habit> findByOwnerIdAndCategoryOrderByCreatedAtAsc(Long ownerId, String category);
    Optional<Habit> findByIdAndOwnerId(Long id, Long ownerId);
}
