package com.habittracker.habit.domain.exception;

public class HabitNotFoundException extends RuntimeException {

    public HabitNotFoundException(long id) {
        super("Habit with id " + id + " not found");
    }
}
