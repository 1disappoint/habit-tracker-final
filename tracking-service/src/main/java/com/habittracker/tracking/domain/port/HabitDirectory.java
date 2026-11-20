package com.habittracker.tracking.domain.port;

/**
 * Порт доступа к справочнику привычек (владелец данных — habit-service).
 */
public interface HabitDirectory {

    boolean habitExists(long habitId);
}
