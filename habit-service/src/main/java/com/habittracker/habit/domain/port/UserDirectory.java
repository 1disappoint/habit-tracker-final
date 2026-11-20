package com.habittracker.habit.domain.port;

/**
 * Порт доступа к справочнику пользователей.
 * Домену habit-service не важно, что за ним стоит Feign, REST или кэш.
 */
public interface UserDirectory {

    boolean userExists(long userId);
}
