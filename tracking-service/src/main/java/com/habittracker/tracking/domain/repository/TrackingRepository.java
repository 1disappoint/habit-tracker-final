package com.habittracker.tracking.domain.repository;

import com.habittracker.tracking.domain.exception.HabitNotExistsException;
import com.habittracker.tracking.domain.exception.TrackingAlreadyExistsException;
import com.habittracker.tracking.domain.model.Tracking;

import java.time.LocalDate;
import java.util.List;

/**
 * Порт хранилища отметок.
 */
public interface TrackingRepository {

    List<Tracking> findAll();

    List<Tracking> findByHabitId(long habitId);

    /**
     * @throws TrackingAlreadyExistsException если привычка уже отмечена в этот день
     * @throws HabitNotExistsException        если привычки не существует
     */
    Tracking create(long habitId, LocalDate completedOn);
}
