package com.habittracker.tracking.infrastructure.client;

import com.habittracker.tracking.domain.exception.HabitServiceUnavailableException;
import com.habittracker.tracking.domain.port.HabitDirectory;
import feign.FeignException;
import org.springframework.stereotype.Component;

/**
 * Адаптер порта {@link HabitDirectory} поверх Feign.
 */
@Component
public class FeignHabitDirectory implements HabitDirectory {

    private final HabitClient habitClient;

    public FeignHabitDirectory(HabitClient habitClient) {
        this.habitClient = habitClient;
    }

    @Override
    public boolean habitExists(long habitId) {
        try {
            habitClient.getHabit(habitId);
            return true;
        } catch (FeignException.NotFound notFound) {
            return false;
        } catch (FeignException exception) {
            throw new HabitServiceUnavailableException(exception);
        }
    }
}
