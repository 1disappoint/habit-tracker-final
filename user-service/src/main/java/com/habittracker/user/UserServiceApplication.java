package com.habittracker.user;

import com.habittracker.user.config.AppConfig;
import com.habittracker.user.db.Database;
import com.habittracker.user.error.ApiException;
import com.habittracker.user.error.ErrorResponse;
import com.habittracker.user.repository.UserRepository;
import com.habittracker.user.web.UserController;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class UserServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(UserServiceApplication.class);

    private UserServiceApplication() {
    }

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        Database database = new Database(config);
        UserRepository repository = new UserRepository(database);
        UserController controller = new UserController(repository);

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
            log.error("Unhandled error in user-service", exception);
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        });
        app.error(404, ctx -> ctx.status(404).json(new ErrorResponse("Resource not found")));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping user-service");
            app.stop();
            database.close();
        }, "user-service-shutdown"));

        app.start("0.0.0.0", config.port());
        log.info("User service listening on port {}", config.port());
    }
}
