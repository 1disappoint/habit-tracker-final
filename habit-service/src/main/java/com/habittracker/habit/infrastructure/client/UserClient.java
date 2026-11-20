package com.habittracker.habit.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Декларативный HTTP-клиент к user-service.
 */
@FeignClient(name = "user-service", url = "${clients.user-service.url}")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDetails getUser(@PathVariable("id") long id);
}
