package com.habittracker.habit.domain.repository;

import com.habittracker.habit.domain.exception.UserNotExistsException;
import com.habittracker.habit.domain.model.Habit;

import java.util.List;
import java.util.Optional;

/**
 * Порт хранилища привычек. Домен ничего не знает о jOOQ и SQL.
 */
public interface HabitRepository {

    List<Habit> findAll();

    Optional<Habit> findById(long id);

    /**
     * @throws UserNotExistsException если пользователь не существует
     */
    Habit create(long userId, String title);
}
