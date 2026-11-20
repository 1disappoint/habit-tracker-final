package com.habittracker.user.application.dto;

import com.habittracker.user.domain.model.User;

/**
 * Модель ответа прикладного слоя. Контроллер ничего не маппит — только отдаёт результат.
 */
public record UserResponse(long id, String name, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), user.email());
    }
}
