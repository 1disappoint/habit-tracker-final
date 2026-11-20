package com.habittracker.tracking.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "habit-service", url = "${clients.habit-service.url}")
public interface HabitClient {

    @GetMapping("/habits/{id}")
    HabitDetails getHabit(@PathVariable("id") long id);
}
