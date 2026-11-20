package com.habittracker.habit.infrastructure.client;

import com.habittracker.habit.domain.exception.UserServiceUnavailableException;
import com.habittracker.habit.domain.port.UserDirectory;
import feign.FeignException;
import org.springframework.stereotype.Component;

/**
 * Адаптер порта {@link UserDirectory} поверх Feign.
 */
@Component
public class FeignUserDirectory implements UserDirectory {

    private final UserClient userClient;

    public FeignUserDirectory(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public boolean userExists(long userId) {
        try {
            userClient.getUser(userId);
            return true;
        } catch (FeignException.NotFound notFound) {
            return false;
        } catch (FeignException exception) {
            throw new UserServiceUnavailableException(exception);
        }
    }
}
