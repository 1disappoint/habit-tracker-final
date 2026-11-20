package com.habittracker.tracking.api;

import com.habittracker.tracking.api.dto.CreateTrackingRequest;
import com.habittracker.tracking.application.TrackingApplicationService;
import com.habittracker.tracking.application.dto.TrackingResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP-адаптер: только маршруты и разбор запроса. Вся логика — в {@link TrackingApplicationService}.
 */
@RestController
public class TrackingController {

    private final TrackingApplicationService trackingService;

    public TrackingController(TrackingApplicationService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/trackings")
    public List<TrackingResponse> findAll(
            @RequestParam(required = false)
            @Positive(message = "Query parameter 'habitId' must be a positive integer")
            Long habitId) {
        return trackingService.findAll(habitId);
    }

    @PostMapping("/trackings/create")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingResponse create(@Valid @RequestBody CreateTrackingRequest request) {
        return trackingService.create(request.habitId(), request.completedOn());
    }
}
