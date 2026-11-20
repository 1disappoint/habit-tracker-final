package com.habittracker.habit.application.dto;

import com.habittracker.habit.domain.model.Habit;

public record HabitResponse(long id, long userId, String title) {

    public static HabitResponse from(Habit habit) {
        return new HabitResponse(habit.id(), habit.userId(), habit.title());
    }
}
