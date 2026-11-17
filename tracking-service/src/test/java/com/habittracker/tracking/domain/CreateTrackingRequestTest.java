package com.habittracker.tracking.domain;

import com.habittracker.tracking.error.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateTrackingRequestTest {

    @Test
    void acceptsValidRequest() {
        CreateTrackingRequest request = new CreateTrackingRequest(1, "2024-01-15");

        assertDoesNotThrow(request::validate);
        assertEquals(LocalDate.of(2024, 1, 15), request.date());
    }

    @Test
    void rejectsNonPositiveHabitId() {
        CreateTrackingRequest request = new CreateTrackingRequest(0, "2024-01-15");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void rejectsInvalidDateFormat() {
        CreateTrackingRequest request = new CreateTrackingRequest(1, "15.01.2024");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void rejectsBlankDate() {
        CreateTrackingRequest request = new CreateTrackingRequest(1, "   ");

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }

    @Test
    void rejectsFutureDate() {
        CreateTrackingRequest request = new CreateTrackingRequest(
                1,
                LocalDate.now().plusDays(1).toString()
        );

        ApiException exception = assertThrows(ApiException.class, request::validate);

        assertEquals(400, exception.status());
    }
}
