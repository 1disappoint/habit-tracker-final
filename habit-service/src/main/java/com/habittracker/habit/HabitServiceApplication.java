package com.habittracker.habit;

import com.habittracker.habit.config.AppConfig;
import com.habittracker.habit.db.Database;
import com.habittracker.habit.error.ApiException;
import com.habittracker.habit.error.ErrorResponse;
import com.habittracker.habit.repository.HabitRepository;
import com.habittracker.habit.web.HabitController;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class HabitServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(HabitServiceApplication.class);

    private HabitServiceApplication() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        Database database = new Database(config);
        HabitRepository repository = new HabitRepository(database);
        HabitController controller = new HabitController(repository);

        Javalin app = Javalin.create(javalin -> {
            javalin.showJavalinBanner = false;
            javalin.jsonMapper(new JavalinJackson());
        });

        app.get("/health", ctx -> {
            if (!database.isHealthy()) {
                throw new ApiException(503, "Database is unavailable");
            }
            ctx.json(Map.of("status", "UP"));
        });

        controller.register(app);

        app.exception(ApiException.class, (exception, ctx) ->
                ctx.status(exception.status()).json(new ErrorResponse(exception.getMessage())));
        app.exception(Exception.class, (exception, ctx) -> {
            log.error("Unhandled error in habit-service", exception);
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        });
        app.error(404, ctx -> ctx.status(404).json(new ErrorResponse("Resource not found")));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping habit-service");
            app.stop();
            database.close();
        }, "habit-service-shutdown"));

        app.start("0.0.0.0", config.port());
        log.info("Habit service listening on port {}", config.port());
    }
}
