package com.habittracker.tracking.repository;

import com.habittracker.tracking.db.Database;
import com.habittracker.tracking.domain.CreateTrackingRequest;
import com.habittracker.tracking.domain.Tracking;
import com.habittracker.tracking.error.ApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class TrackingRepository {

    private static final String COLUMNS = "SELECT id, habit_id, completed_on FROM trackings";
    private static final String FIND_ALL_SQL = COLUMNS + " ORDER BY id";
    private static final String FIND_BY_HABIT_SQL = COLUMNS + " WHERE habit_id = ? ORDER BY id";
    private static final String INSERT_SQL =
            "INSERT INTO trackings (habit_id, completed_on) VALUES (?, ?) RETURNING id";

    private final Database database;

    public TrackingRepository(Database database) {
        this.database = database;
    }

    public List<Tracking> findAll() {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            return collect(resultSet);
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to load trackings", exception);
        }
    }

    public List<Tracking> findByHabitId(int habitId) {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_HABIT_SQL)) {

            statement.setInt(1, habitId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return collect(resultSet);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to load trackings for habit " + habitId, exception);
        }
    }

    public Tracking create(CreateTrackingRequest request) {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setInt(1, request.habitId());
            statement.setObject(2, request.date());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException("Database did not return generated tracking id");
                }
                return new Tracking(
                        resultSet.getInt("id"),
                        request.habitId(),
                        request.date().toString()
                );
            }
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new ApiException(
                        409,
                        "Habit " + request.habitId() + " is already tracked on " + request.date()
                );
            }
            if ("23503".equals(exception.getSQLState())) {
                throw new ApiException(422, "Habit with id " + request.habitId() + " does not exist");
            }
            throw new DataAccessException("Failed to create tracking", exception);
        }
    }

    private static List<Tracking> collect(ResultSet resultSet) throws SQLException {
        List<Tracking> trackings = new ArrayList<>();
        while (resultSet.next()) {
            trackings.add(new Tracking(
                    resultSet.getInt("id"),
                    resultSet.getInt("habit_id"),
                    resultSet.getDate("completed_on").toLocalDate().toString()
            ));
        }
        return trackings;
    }
}
