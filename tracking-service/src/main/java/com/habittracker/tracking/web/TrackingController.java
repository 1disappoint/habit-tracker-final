package com.habittracker.tracking.web;

import com.habittracker.tracking.client.HabitServiceClient;
import com.habittracker.tracking.domain.CreateTrackingRequest;
import com.habittracker.tracking.error.ApiException;
import com.habittracker.tracking.repository.TrackingRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public final class TrackingController {

    private final TrackingRepository repository;
    private final HabitServiceClient habitServiceClient;

    public TrackingController(TrackingRepository repository, HabitServiceClient habitServiceClient) {
        this.repository = repository;
        this.habitServiceClient = habitServiceClient;
    }

    public void register(Javalin app) {
        app.get("/trackings", this::findAll);
        app.post("/trackings", this::create);
    }

    private void findAll(Context ctx) {
        String habitId = ctx.queryParam("habitId");
        if (habitId == null || habitId.isBlank()) {
            ctx.json(repository.findAll());
            return;
        }
        ctx.json(repository.findByHabitId(parseHabitIdParam(habitId)));
    }

    private void create(Context ctx) {
        CreateTrackingRequest request = parseBody(ctx);
        request.validate();

        if (!habitServiceClient.exists(request.habitId())) {
            throw new ApiException(422, "Habit with id " + request.habitId() + " does not exist");
        }

        ctx.status(201).json(repository.create(request));
    }

    private static int parseHabitIdParam(String value) {
        try {
            int habitId = Integer.parseInt(value.trim());
            if (habitId <= 0) {
                throw new NumberFormatException();
            }
            return habitId;
        } catch (NumberFormatException exception) {
            throw new ApiException(400, "Query parameter 'habitId' must be a positive integer");
        }
    }

    private static CreateTrackingRequest parseBody(Context ctx) {
        try {
            CreateTrackingRequest request = ctx.bodyAsClass(CreateTrackingRequest.class);
            if (request == null) {
                throw new ApiException(400, "Request body is required");
            }
            return request;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(400, "Malformed JSON request body");
        }
    }
}
