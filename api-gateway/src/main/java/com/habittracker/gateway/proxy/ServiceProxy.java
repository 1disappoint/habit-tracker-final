package com.habittracker.gateway.proxy;

import com.habittracker.gateway.config.GatewayConfig;
import com.habittracker.gateway.error.ApiException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Set;

public final class ServiceProxy {

    private static final Logger log = LoggerFactory.getLogger(ServiceProxy.class);
    private static final Set<String> METHODS_WITH_BODY = Set.of("POST", "PUT", "PATCH");

    private final HttpClient httpClient;
    private final Duration requestTimeout;

    public ServiceProxy(GatewayConfig config) {
        this.requestTimeout = config.requestTimeout();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public void forward(Context ctx, String serviceUrl, String path, String method) {
        String url = buildUrl(serviceUrl, path, ctx.queryString());

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
                .timeout(requestTimeout)
                .header("Accept", "application/json");

        if (METHODS_WITH_BODY.contains(method)) {
            String contentType = ctx.contentType() == null ? "application/json" : ctx.contentType();
            requestBuilder
                    .header("Content-Type", contentType)
                    .method(method, HttpRequest.BodyPublishers.ofString(ctx.body()));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            ctx.status(response.statusCode());
            ctx.contentType(response.headers().firstValue("Content-Type").orElse("application/json"));
            ctx.result(response.body());
        } catch (HttpTimeoutException exception) {
            log.warn("Upstream request timed out: {} {}", method, url);
            throw new ApiException(504, "Upstream service timed out");
        } catch (IOException exception) {
            log.warn("Upstream service is unavailable: {} {} ({})", method, url, exception.getMessage());
            throw new ApiException(502, "Upstream service is unavailable");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(502, "Request to upstream service was interrupted");
        }
    }

    private static String buildUrl(String serviceUrl, String path, String queryString) {
        String url = serviceUrl + path;
        return queryString == null || queryString.isBlank() ? url : url + "?" + queryString;
    }
}
