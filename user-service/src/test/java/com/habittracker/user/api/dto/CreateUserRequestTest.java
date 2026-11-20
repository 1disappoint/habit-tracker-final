package com.habittracker.user.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUserRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void normalizesNameAndEmail() {
        CreateUserRequest request = new CreateUserRequest("  Ivan  ", "  IVAN@TEST.COM  ");

        assertEquals("Ivan", request.name());
        assertEquals("ivan@test.com", request.email());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsBlankName() {
        CreateUserRequest request = new CreateUserRequest("   ", "ivan@test.com");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsInvalidEmail() {
        CreateUserRequest request = new CreateUserRequest("Ivan", "not-an-email");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsValidRequest() {
        CreateUserRequest request = new CreateUserRequest("Ivan", "ivan@test.com");

        assertTrue(validator.validate(request).isEmpty());
    }
}
