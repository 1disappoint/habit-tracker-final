package com.habittracker.tracking.domain.exception;

public class FutureTrackingDateException extends RuntimeException {

    public FutureTrackingDateException(Object completedOn) {
        super("Field 'completedOn' cannot be in the future: " + completedOn);
    }
}
