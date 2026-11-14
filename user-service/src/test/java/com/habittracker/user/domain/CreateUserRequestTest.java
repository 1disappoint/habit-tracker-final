package com.habittracker.user.domain;

import com.habittracker.user.error.ApiException;
import io.javalin.json.JavalinJackson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateUserRequestTest {

    @Test
    void deserializesFromJson() {
        JavalinJackson mapper = new JavalinJackson();

        CreateUserRequest request = mapper.fromJsonString(
                "{\"name\":\"Ivan\",\"email\":\"ivan@test.com\"}",
                CreateUserRequest.class
        );

        assertEquals("Ivan", request.name());
        assertEquals("ivan@test.com", request.email());
    }

    @Test
    void normalizesNameAndEmail() {
        CreateUserRequest request = new CreateUserRequest("  Ivan  ", "  IVAN@TEST.COM  ");

        request.validate();

        assertEquals("Ivan", request.name());
        assertEquals("ivan@test.com", request.email());
    }

    @Test
    void rejectsBlankName() {
        CreateUserRequest request = new CreateUserRequest("   ", "ivan@test.com");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void rejectsInvalidEmail() {
        CreateUserRequest request = new CreateUserRequest("Ivan", "not-an-email");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void acceptsValidRequest() {
        CreateUserRequest request = new CreateUserRequest("Ivan", "ivan@test.com");

        assertDoesNotThrow(request::validate);
    }
}
