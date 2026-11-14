package com.habittracker.habit.db;

import com.habittracker.habit.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public final class Database implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private final HikariDataSource dataSource;

    public Database(AppConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.dbUrl());
        hikariConfig.setUsername(config.dbUser());
        hikariConfig.setPassword(config.dbPassword());
        hikariConfig.setMaximumPoolSize(config.dbPoolSize());
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(config.dbConnectionTimeout().toMillis());
        hikariConfig.setPoolName("habit-service-pool");
        hikariConfig.setInitializationFailTimeout(-1);

        this.dataSource = new HikariDataSource(hikariConfig);
        log.info("Database connection pool initialized for {}", config.dbUrl());
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean isHealthy() {
        try (Connection connection = connection()) {
            return connection.isValid(2);
        } catch (SQLException exception) {
            log.warn("Database health check failed: {}", exception.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
