package com.habittracker.habit.web;

import com.habittracker.habit.domain.CreateHabitRequest;
import com.habittracker.habit.error.ApiException;
import com.habittracker.habit.repository.HabitRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public final class HabitController {

    private final HabitRepository repository;

    public HabitController(HabitRepository repository) {
        this.repository = repository;
    }

    public void register(Javalin app) {
        app.get("/habits", this::findAll);
        app.post("/habits", this::create);
    }

    private void findAll(Context ctx) {
        ctx.json(repository.findAll());
    }

    private void create(Context ctx) {
        CreateHabitRequest request = parseBody(ctx);
        request.validate();
        ctx.status(201).json(repository.create(request));
    }

    private static CreateHabitRequest parseBody(Context ctx) {
        try {
            CreateHabitRequest request = ctx.bodyAsClass(CreateHabitRequest.class);
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
