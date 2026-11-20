package com.habittracker.habit.api;

import com.habittracker.habit.api.dto.CreateHabitRequest;
import com.habittracker.habit.application.HabitApplicationService;
import com.habittracker.habit.application.dto.HabitResponse;
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
 * HTTP-адаптер: только маршруты и разбор запроса. Вся логика — в {@link HabitApplicationService}.
 */
@RestController
public class HabitController {

    private final HabitApplicationService habitService;

    public HabitController(HabitApplicationService habitService) {
        this.habitService = habitService;
    }

    @GetMapping("/habits")
    public List<HabitResponse> findAll() {
        return habitService.findAll();
    }

    @GetMapping("/habits/{id}")
    public HabitResponse findById(@PathVariable long id) {
        return habitService.findById(id);
    }

    @PostMapping("/habits/create")
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse create(@Valid @RequestBody CreateHabitRequest request) {
        return habitService.create(request.userId(), request.title());
    }
}
