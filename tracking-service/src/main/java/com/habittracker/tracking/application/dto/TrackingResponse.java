package com.habittracker.tracking.application.dto;

import com.habittracker.tracking.domain.model.Tracking;

import java.time.LocalDate;

public record TrackingResponse(long id, long habitId, LocalDate completedOn) {

    public static TrackingResponse from(Tracking tracking) {
        return new TrackingResponse(tracking.id(), tracking.habitId(), tracking.completedOn());
    }
}
