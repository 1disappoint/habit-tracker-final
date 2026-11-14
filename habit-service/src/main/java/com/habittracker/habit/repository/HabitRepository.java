package com.habittracker.habit.repository;

import com.habittracker.habit.db.Database;
import com.habittracker.habit.domain.CreateHabitRequest;
import com.habittracker.habit.domain.Habit;
import com.habittracker.habit.error.ApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class HabitRepository {

    private static final String FIND_ALL_SQL = "SELECT id, user_id, title FROM habits ORDER BY id";
    private static final String INSERT_SQL = "INSERT INTO habits (user_id, title) VALUES (?, ?) RETURNING id";

    private final Database database;

    public HabitRepository(Database database) {
        this.database = database;
    }

    public List<Habit> findAll() {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<Habit> habits = new ArrayList<>();
            while (resultSet.next()) {
                habits.add(new Habit(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("title")
                ));
            }
            return habits;
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to load habits", exception);
        }
    }

    public Habit create(CreateHabitRequest request) {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setInt(1, request.userId());
            statement.setString(2, request.title());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException("Database did not return generated habit id");
                }
                return new Habit(resultSet.getInt("id"), request.userId(), request.title());
            }
        } catch (SQLException exception) {
            if ("23503".equals(exception.getSQLState())) {
                throw new ApiException(422, "User with id " + request.userId() + " does not exist");
            }
            throw new DataAccessException("Failed to create habit", exception);
        }
    }
}
