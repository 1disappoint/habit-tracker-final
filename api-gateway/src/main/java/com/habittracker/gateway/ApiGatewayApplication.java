package com.habittracker.gateway;

import com.habittracker.gateway.config.GatewayConfig;
import com.habittracker.gateway.error.ApiException;
import com.habittracker.gateway.error.ErrorResponse;
import com.habittracker.gateway.proxy.ServiceProxy;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayApplication.class);

    private ApiGatewayApplication() {
    }

    public static void main(String[] args) {
        GatewayConfig config = GatewayConfig.fromEnvironment();
        ServiceProxy proxy = new ServiceProxy(config);

        Javalin app = Javalin.create(javalin -> {
            javalin.showJavalinBanner = false;
            javalin.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
                if ("*".equals(config.corsAllowedOrigin())) {
                    rule.anyHost();
                } else {
                    rule.allowHost(config.corsAllowedOrigin());
                }
            }));
        });

        app.get("/health", ctx -> ctx.json(new HealthResponse("UP")));

        app.get("/users", ctx -> proxy.forward(ctx, config.userServiceUrl(), "/users", "GET"));
        app.post("/users", ctx -> proxy.forward(ctx, config.userServiceUrl(), "/users", "POST"));

        app.get("/habits", ctx -> proxy.forward(ctx, config.habitServiceUrl(), "/habits", "GET"));
        app.post("/habits", ctx -> proxy.forward(ctx, config.habitServiceUrl(), "/habits", "POST"));

        app.exception(ApiException.class, (exception, ctx) ->
                ctx.status(exception.status()).json(new ErrorResponse(exception.getMessage())));
        app.exception(Exception.class, (exception, ctx) -> {
            log.error("Unhandled gateway error", exception);
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        });
        app.error(404, ctx -> ctx.status(404).json(new ErrorResponse("Resource not found")));

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop, "gateway-shutdown"));

        app.start("0.0.0.0", config.port());
        log.info("API Gateway listening on port {}", config.port());
    }

    public record HealthResponse(String status) {
    }
}
