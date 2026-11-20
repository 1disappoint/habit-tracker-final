package com.habittracker.user.api;

import com.habittracker.user.api.dto.CreateUserRequest;
import com.habittracker.user.application.UserApplicationService;
import com.habittracker.user.application.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP-адаптер: только маршруты и разбор запроса. Вся логика — в {@link UserApplicationService}.
 */
@RestController
public class UserController {

    private final UserApplicationService userService;

    public UserController(UserApplicationService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public UserResponse findById(@PathVariable long id) {
        return userService.findById(id);
    }

    @PostMapping("/users/create")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request.name(), request.email());
    }
}
