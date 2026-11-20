package com.habittracker.habit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HabitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitServiceApplication.class, args);
    }
}
