package com.habittracker.tracking.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateTrackingRequestTest {

    private static final ObjectMapper JSON = new ObjectMapper().registerModule(new JavaTimeModule());

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
    void rejectsNonPositiveHabitId() throws JsonProcessingException {
        // Значение приходит из JSON, как в реальном запросе: ограничение @Positive проверяет валидатор.
        CreateTrackingRequest request = JSON.readValue(
                "{\"habitId\":0,\"completedOn\":\"2026-01-15\"}",
                CreateTrackingRequest.class
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsMissingDate() throws JsonProcessingException {
        CreateTrackingRequest request = JSON.readValue(
                "{\"habitId\":1}",
                CreateTrackingRequest.class
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsValidRequest() {
        CreateTrackingRequest request = new CreateTrackingRequest(1, LocalDate.of(2026, 1, 15));

        assertTrue(validator.validate(request).isEmpty());
    }
}
