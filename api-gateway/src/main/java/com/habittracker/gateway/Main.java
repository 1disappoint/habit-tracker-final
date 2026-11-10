package com.habittracker.gateway;

import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        
        String userServiceUrl = System.getenv().getOrDefault("USER_SERVICE_URL", "http://localhost:8081");
        String habitServiceUrl = System.getenv().getOrDefault("HABIT_SERVICE_URL", "http://localhost:8082");

        HttpClient client = HttpClient.newHttpClient();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start("0.0.0.0", port);

        System.out.println("API Gateway started on port " + port);

        // Прокси для user-service
        app.get("/users", ctx -> proxyRequest(ctx, client, userServiceUrl + "/users", "GET", null));
        app.post("/users", ctx -> proxyRequest(ctx, client, userServiceUrl + "/users", "POST", ctx.body()));

        // Прокси для habit-service
        app.get("/habits", ctx -> proxyRequest(ctx, client, habitServiceUrl + "/habits", "GET", null));
        app.post("/habits", ctx -> proxyRequest(ctx, client, habitServiceUrl + "/habits", "POST", ctx.body()));
    }

    private static void proxyRequest(io.javalin.http.Context ctx, HttpClient client, String url, String method, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if ("POST".equals(method)) {
                builder.POST(HttpRequest.BodyPublishers.ofString(body));
            } else {
                builder.GET();
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            ctx.status(response.statusCode()).result(response.body());
        } catch (Exception e) {
            ctx.status(500).result("Gateway Error: " + e.getMessage());
        }
    }
}