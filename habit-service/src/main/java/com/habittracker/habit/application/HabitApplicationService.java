package com.habittracker.habit.application;

import com.habittracker.habit.application.dto.HabitResponse;
import com.habittracker.habit.domain.exception.HabitNotFoundException;
import com.habittracker.habit.domain.exception.UserNotExistsException;
import com.habittracker.habit.domain.port.UserDirectory;
import com.habittracker.habit.domain.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HabitApplicationService {

    private final HabitRepository habitRepository;
    private final UserDirectory userDirectory;

    public HabitApplicationService(HabitRepository habitRepository, UserDirectory userDirectory) {
        this.habitRepository = habitRepository;
        this.userDirectory = userDirectory;
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> findAll() {
        return habitRepository.findAll().stream()
                .map(HabitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitResponse findById(long id) {
        return habitRepository.findById(id)
                .map(HabitResponse::from)
                .orElseThrow(() -> new HabitNotFoundException(id));
    }

    @Transactional
    public HabitResponse create(long userId, String title) {
        if (!userDirectory.userExists(userId)) {
            throw new UserNotExistsException(userId);
        }
        return HabitResponse.from(habitRepository.create(userId, title));
    }
}
