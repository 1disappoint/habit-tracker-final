package com.habittracker.tracking.domain.exception;

import java.time.LocalDate;

public class TrackingAlreadyExistsException extends RuntimeException {

    public TrackingAlreadyExistsException(long habitId, LocalDate completedOn) {
        super("Habit " + habitId + " is already tracked on " + completedOn);
    }
}
