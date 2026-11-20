package com.habittracker.habit.domain.exception;

public class UserNotExistsException extends RuntimeException {

    public UserNotExistsException(long userId) {
        super("User with id " + userId + " does not exist");
    }
}
