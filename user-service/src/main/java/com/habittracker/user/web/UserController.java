package com.habittracker.user.web;

import com.habittracker.user.domain.CreateUserRequest;
import com.habittracker.user.error.ApiException;
import com.habittracker.user.repository.UserRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;

public final class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    public void register(Javalin app) {
        app.get("/users", this::findAll);
        app.post("/users", this::create);
    }

    private void findAll(Context ctx) {
        ctx.json(repository.findAll());
    }

    private void create(Context ctx) {
        CreateUserRequest request = parseBody(ctx);
        request.validate();
        ctx.status(201).json(repository.create(request));
    }

    private static CreateUserRequest parseBody(Context ctx) {
        try {
            CreateUserRequest request = ctx.bodyAsClass(CreateUserRequest.class);
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
