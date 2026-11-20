package com.habittracker.user.domain.repository;

import com.habittracker.user.domain.exception.UserAlreadyExistsException;
import com.habittracker.user.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Порт хранилища пользователей. Домен ничего не знает о jOOQ и SQL.
 */
public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(long id);

    /**
     * @throws UserAlreadyExistsException если email уже занят
     */
    User create(String name, String email);
}
