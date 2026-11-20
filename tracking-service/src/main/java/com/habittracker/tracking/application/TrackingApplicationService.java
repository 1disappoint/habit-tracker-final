package com.habittracker.tracking.application;

import com.habittracker.tracking.application.dto.TrackingResponse;
import com.habittracker.tracking.domain.exception.FutureTrackingDateException;
import com.habittracker.tracking.domain.exception.HabitNotExistsException;
import com.habittracker.tracking.domain.model.Tracking;
import com.habittracker.tracking.domain.port.HabitDirectory;
import com.habittracker.tracking.domain.repository.TrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TrackingApplicationService {

    /**
     * Допуск на часовые пояса: клиент в UTC+14 может прислать «сегодня»,
     * которое на день впереди даты сервера (UTC).
     */
    private static final long FUTURE_DATE_TOLERANCE_DAYS = 1;

    private final TrackingRepository trackingRepository;
    private final HabitDirectory habitDirectory;

    public TrackingApplicationService(TrackingRepository trackingRepository, HabitDirectory habitDirectory) {
        this.trackingRepository = trackingRepository;
        this.habitDirectory = habitDirectory;
    }

    @Transactional(readOnly = true)
    public List<TrackingResponse> findAll(Long habitId) {
        List<Tracking> trackings = habitId == null
                ? trackingRepository.findAll()
                : trackingRepository.findByHabitId(habitId);

        return trackings.stream()
                .map(TrackingResponse::from)
                .toList();
    }

    @Transactional
    public TrackingResponse create(long habitId, LocalDate completedOn) {
        LocalDate latestAllowed = LocalDate.now(ZoneOffset.UTC).plusDays(FUTURE_DATE_TOLERANCE_DAYS);
        if (completedOn.isAfter(latestAllowed)) {
            throw new FutureTrackingDateException(completedOn);
        }
        if (!habitDirectory.habitExists(habitId)) {
            throw new HabitNotExistsException(habitId);
        }
        return TrackingResponse.from(trackingRepository.create(habitId, completedOn));
    }
}
