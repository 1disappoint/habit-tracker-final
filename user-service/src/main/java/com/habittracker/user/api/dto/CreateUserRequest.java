package com.habittracker.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record CreateUserRequest(

        @NotBlank(message = "Field 'name' is required")
        @Size(max = 100, message = "Field 'name' must not exceed 100 characters")
        String name,

        @NotBlank(message = "Field 'email' is required")
        @Size(max = 255, message = "Field 'email' must not exceed 255 characters")
        @Email(message = "Field 'email' must be a valid email address")
        String email
) {

    public CreateUserRequest {
        name = name == null ? null : name.trim();
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
