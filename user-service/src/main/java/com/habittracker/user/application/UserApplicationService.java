package com.habittracker.user.application;

import com.habittracker.user.application.dto.UserResponse;
import com.habittracker.user.domain.exception.UserNotFoundException;
import com.habittracker.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Прикладной слой: сценарии использования, транзакционные границы и маппинг в модели ответа.
 */
@Service
public class UserApplicationService {

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public UserResponse create(String name, String email) {
        return UserResponse.from(userRepository.create(name, email));
    }
}
