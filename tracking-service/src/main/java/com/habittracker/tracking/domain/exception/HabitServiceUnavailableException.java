package com.habittracker.tracking.domain.exception;

public class HabitServiceUnavailableException extends RuntimeException {

    public HabitServiceUnavailableException(Throwable cause) {
        super("habit-service is unavailable", cause);
    }
}
