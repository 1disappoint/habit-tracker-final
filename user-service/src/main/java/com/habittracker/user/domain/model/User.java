package com.habittracker.user.domain.model;

/**
 * Пользователь — корень агрегата user-service.
 */
public record User(long id, String name, String email) {
}
