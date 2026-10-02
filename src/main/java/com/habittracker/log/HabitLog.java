package com.habittracker.log;

import com.habittracker.habit.Habit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "habit_logs", uniqueConstraints = @UniqueConstraint(columnNames = {"habit_id", "date"}))
@Getter
@Setter
@NoArgsConstructor
public class HabitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, updatable = false)
    private Instant completedAt = Instant.now();

    public HabitLog(Habit habit, LocalDate date) {
        this.habit = habit;
        this.date = date;
    }
}
