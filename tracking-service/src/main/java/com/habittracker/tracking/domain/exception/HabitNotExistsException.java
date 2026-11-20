package com.habittracker.tracking.domain.exception;

public class HabitNotExistsException extends RuntimeException {

    public HabitNotExistsException(long habitId) {
        super("Habit with id " + habitId + " does not exist");
    }
}
