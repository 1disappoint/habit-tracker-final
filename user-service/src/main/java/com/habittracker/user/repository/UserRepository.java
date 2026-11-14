package com.habittracker.user.repository;

import com.habittracker.user.db.Database;
import com.habittracker.user.domain.CreateUserRequest;
import com.habittracker.user.domain.User;
import com.habittracker.user.error.ApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class UserRepository {

    private static final String FIND_ALL_SQL = "SELECT id, name, email FROM users ORDER BY id";
    private static final String INSERT_SQL = "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id";

    private final Database database;

    public UserRepository(Database database) {
        this.database = database;
    }

    public List<User> findAll() {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")
                ));
            }
            return users;
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to load users", exception);
        }
    }

    public User create(CreateUserRequest request) {
        try (Connection connection = database.connection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, request.name());
            statement.setString(2, request.email());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException("Database did not return generated user id");
                }
                return new User(resultSet.getInt("id"), request.name(), request.email());
            }
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new ApiException(409, "User with email '" + request.email() + "' already exists");
            }
            throw new DataAccessException("Failed to create user", exception);
        }
    }
}
