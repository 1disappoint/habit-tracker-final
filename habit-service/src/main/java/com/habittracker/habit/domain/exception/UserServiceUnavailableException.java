package com.habittracker.habit.domain.exception;

public class UserServiceUnavailableException extends RuntimeException {

    public UserServiceUnavailableException(Throwable cause) {
        super("user-service is unavailable", cause);
    }
}
