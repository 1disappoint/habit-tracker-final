package com.habittracker.habit.infrastructure.persistence;

import com.habittracker.habit.domain.exception.UserNotExistsException;
import com.habittracker.habit.domain.model.Habit;
import com.habittracker.habit.domain.repository.HabitRepository;
import com.habittracker.habit.infrastructure.persistence.jooq.tables.records.HabitsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.habittracker.habit.infrastructure.persistence.jooq.tables.Habits.HABITS;

/**
 * Хранилище привычек в PostgreSQL (jOOQ).
 */
@Repository
public class PostgresHabitRepository implements HabitRepository {

    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private final DSLContext dsl;

    public PostgresHabitRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Habit> findAll() {
        return dsl.selectFrom(HABITS)
                .orderBy(HABITS.ID)
                .fetch(PostgresHabitRepository::toHabit);
    }

    @Override
    public Optional<Habit> findById(long id) {
        return dsl.selectFrom(HABITS)
                .where(HABITS.ID.eq(Math.toIntExact(id)))
                .fetchOptional(PostgresHabitRepository::toHabit);
    }

    @Override
    public Habit create(long userId, String title) {
        try {
            HabitsRecord inserted = dsl.insertInto(HABITS)
                    .set(HABITS.USER_ID, Math.toIntExact(userId))
                    .set(HABITS.TITLE, title)
                    .returning(HABITS.ID)
                    .fetchOne();

            if (inserted == null || inserted.getId() == null) {
                throw new IllegalStateException("PostgreSQL did not return generated id for habits");
            }
            return new Habit(inserted.getId().longValue(), userId, title);
        } catch (RuntimeException exception) {
            if (FOREIGN_KEY_VIOLATION.equals(sqlStateOf(exception))) {
                throw new UserNotExistsException(userId);
            }
            throw exception;
        }
    }

    private static Habit toHabit(HabitsRecord record) {
        return new Habit(record.getId().longValue(), record.getUserId().longValue(), record.getTitle());
    }

    /**
     * Возвращает SQLState первого {@link SQLException} в цепочке причин или {@code null}.
     */
    private static String sqlStateOf(Throwable throwable) {
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException) {
                return sqlException.getSQLState();
            }
        }
        return null;
    }
}
