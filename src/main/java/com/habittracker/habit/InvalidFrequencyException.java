package com.habittracker.habit;

public class InvalidFrequencyException extends RuntimeException {
    public InvalidFrequencyException(String message) {
        super(message);
    }
}
