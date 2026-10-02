package com.habittracker.habit;

public class HabitNotFoundException extends RuntimeException {
    public HabitNotFoundException(Long id) {
        super("No habit found with id " + id);
    }
}
