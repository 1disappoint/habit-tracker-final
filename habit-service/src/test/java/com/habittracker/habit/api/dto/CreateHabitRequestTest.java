package com.habittracker.habit.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateHabitRequestTest {

    private static final ObjectMapper JSON = new ObjectMapper();

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
    void trimsTitle() {
        CreateHabitRequest request = new CreateHabitRequest(1, "  Drink water  ");

        assertEquals("Drink water", request.title());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsNonPositiveUserId() throws JsonProcessingException {
        // Значение приходит из JSON, как в реальном запросе: ограничение @Positive проверяет валидатор.
        CreateHabitRequest request = JSON.readValue(
                "{\"userId\":0,\"title\":\"Drink water\"}",
                CreateHabitRequest.class
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsBlankTitle() {
        CreateHabitRequest request = new CreateHabitRequest(1, "   ");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsValidRequest() {
        CreateHabitRequest request = new CreateHabitRequest(1, "Drink water");

        assertTrue(validator.validate(request).isEmpty());
    }
}
