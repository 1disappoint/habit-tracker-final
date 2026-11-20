package com.habittracker.tracking.domain.model;

import java.time.LocalDate;

/**
 * Отметка о выполнении привычки за конкретный день.
 */
public record Tracking(long id, long habitId, LocalDate completedOn) {
}
