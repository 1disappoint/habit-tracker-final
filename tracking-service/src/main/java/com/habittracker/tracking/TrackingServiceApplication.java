package com.habittracker.tracking;

import com.habittracker.tracking.client.HabitServiceClient;
import com.habittracker.tracking.config.AppConfig;
import com.habittracker.tracking.db.Database;
import com.habittracker.tracking.error.ApiException;
import com.habittracker.tracking.error.ErrorResponse;
import com.habittracker.tracking.repository.TrackingRepository;
import com.habittracker.tracking.web.TrackingController;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class TrackingServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(TrackingServiceApplication.class);

    private TrackingServiceApplication() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        Database database = new Database(config);
        TrackingRepository repository = new TrackingRepository(database);
        HabitServiceClient habitServiceClient = new HabitServiceClient(config);
        TrackingController controller = new TrackingController(repository, habitServiceClient);

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
            log.error("Unhandled error in tracking-service", exception);
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        });
        app.error(404, ctx -> {
            // Не перезаписываем тело, если его уже установил контроллер.
            String existing = ctx.result();
            if (existing != null && existing.trim().startsWith("{")) {
                return;
            }
            ctx.status(404).json(new ErrorResponse("Resource not found"));
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping tracking-service");
            app.stop();
            database.close();
        }, "tracking-service-shutdown"));

        app.start("0.0.0.0", config.port());
        log.info("Tracking service listening on port {}", config.port());
    }
}
