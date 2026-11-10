package com.habittracker.user;

import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Класс пользователя (с пустым конструктором для Jackson)
    public static class User {
        public int id;
        public String name;
        public String email;

        public User() {}
        public User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    // Настройки подключения к базе (читаем из переменных окружения)
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/app");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String DB_PASS = System.getenv().getOrDefault("DB_PASS", "1234");

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
            config.jsonMapper(new io.javalin.json.JavalinJackson());
        }).start("0.0.0.0", port);

        System.out.println("User Service started on port " + port);

        // GET /users - получить всех пользователей из БД
        app.get("/users", ctx -> {
            List<User> users = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    users.add(new User(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("DB Error: " + e.getMessage());
                return;
            }
            ctx.json(users);
        });

        // POST /users - сохранить пользователя в БД
        app.post("/users", ctx -> {
            User newUser = ctx.bodyAsClass(User.class);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, email) VALUES (?, ?) RETURNING id")) {
                
                stmt.setString(1, newUser.name);
                stmt.setString(2, newUser.email);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    newUser.id = rs.getInt(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ctx.status(500).result("DB Error: " + e.getMessage());
                return;
            }
            ctx.status(201).json(newUser);
        });
    }
}