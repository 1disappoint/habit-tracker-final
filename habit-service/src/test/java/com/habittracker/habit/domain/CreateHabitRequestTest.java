package com.habittracker.habit.domain;

import com.habittracker.habit.error.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateHabitRequestTest {

    @Test
    void trimsTitle() {
        CreateHabitRequest request = new CreateHabitRequest(1, "  Drink water  ");

        request.validate();

        assertEquals("Drink water", request.title());
    }

    @Test
    void rejectsNonPositiveUserId() {
        CreateHabitRequest request = new CreateHabitRequest(0, "Drink water");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void rejectsBlankTitle() {
        CreateHabitRequest request = new CreateHabitRequest(1, "   ");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void acceptsValidRequest() {
        CreateHabitRequest request = new CreateHabitRequest(1, "Drink water");

        assertDoesNotThrow(request::validate);
    }
}
