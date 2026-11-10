package com.habittracker.habit;

import io.javalin.Javalin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static class Habit {
        public int id;
        public int userId;
        public String title;

        public Habit() {}
        public Habit(int id, int userId, String title) {
            this.id = id;
            this.userId = userId;
            this.title = title;
        }
    }

    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/app");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DB_PASS = System.getenv().getOrDefault("DB_PASS", "1234");

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8082"));

        // Инициализация таблицы при старте
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS habits (id SERIAL PRIMARY KEY, user_id INT NOT NULL, title VARCHAR(255) NOT NULL)");
        } catch (Exception e) {
            System.out.println("DB Init Error: " + e.getMessage());
        }

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.jsonMapper(new io.javalin.json.JavalinJackson());
        }).start("0.0.0.0", port);

        System.out.println("Habit Service started on port " + port);

        // GET /habits - получить все привычки
        app.get("/habits", ctx -> {
            List<Habit> habits = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM habits");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    habits.add(new Habit(rs.getInt("id"), rs.getInt("user_id"), rs.getString("title")));
                }
            } catch (Exception e) {
                ctx.status(500).result("DB Error: " + e.getMessage());
                return;
            }
            ctx.json(habits);
        });

        // POST /habits - создать привычку
        app.post("/habits", ctx -> {
            Habit newHabit = ctx.bodyAsClass(Habit.class);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO habits (user_id, title) VALUES (?, ?) RETURNING id")) {
                stmt.setInt(1, newHabit.userId);
                stmt.setString(2, newHabit.title);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    newHabit.id = rs.getInt(1);
                }
            } catch (Exception e) {
                ctx.status(500).result("DB Error: " + e.getMessage());
                return;
            }
            ctx.status(201).json(newHabit);
        });
    }
}