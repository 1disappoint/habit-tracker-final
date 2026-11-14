package com.habittracker.user.domain;

import com.habittracker.user.error.ApiException;

import java.util.Locale;
import java.util.regex.Pattern;

public record CreateUserRequest(String name, String email) {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public CreateUserRequest {
        name = name == null ? null : name.trim();
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new ApiException(400, "Field 'name' is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new ApiException(400, "Field 'name' must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        if (email == null || email.isBlank()) {
            throw new ApiException(400, "Field 'email' is required");
        }
        if (email.length() > MAX_EMAIL_LENGTH) {
            throw new ApiException(400, "Field 'email' must not exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(400, "Field 'email' must be a valid email address");
        }
    }
}
