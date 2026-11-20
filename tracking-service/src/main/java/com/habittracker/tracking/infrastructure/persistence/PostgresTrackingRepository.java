package com.habittracker.tracking.infrastructure.persistence;

import com.habittracker.tracking.domain.exception.HabitNotExistsException;
import com.habittracker.tracking.domain.exception.TrackingAlreadyExistsException;
import com.habittracker.tracking.domain.model.Tracking;
import com.habittracker.tracking.domain.repository.TrackingRepository;
import com.habittracker.tracking.infrastructure.persistence.jooq.tables.records.TrackingsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static com.habittracker.tracking.infrastructure.persistence.jooq.tables.Trackings.TRACKINGS;

/**
 * Хранилище отметок о выполнении в PostgreSQL (jOOQ).
 */
@Repository
public class PostgresTrackingRepository implements TrackingRepository {

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private final DSLContext dsl;

    public PostgresTrackingRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Tracking> findAll() {
        return dsl.selectFrom(TRACKINGS)
                .orderBy(TRACKINGS.ID)
                .fetch(PostgresTrackingRepository::toTracking);
    }

    @Override
    public List<Tracking> findByHabitId(long habitId) {
        return dsl.selectFrom(TRACKINGS)
                .where(TRACKINGS.HABIT_ID.eq(Math.toIntExact(habitId)))
                .orderBy(TRACKINGS.ID)
                .fetch(PostgresTrackingRepository::toTracking);
    }

    @Override
    public Tracking create(long habitId, LocalDate completedOn) {
        try {
            TrackingsRecord inserted = dsl.insertInto(TRACKINGS)
                    .set(TRACKINGS.HABIT_ID, Math.toIntExact(habitId))
                    .set(TRACKINGS.COMPLETED_ON, completedOn)
                    .returning(TRACKINGS.ID)
                    .fetchOne();

            if (inserted == null || inserted.getId() == null) {
                throw new IllegalStateException("PostgreSQL did not return generated id for trackings");
            }
            return new Tracking(inserted.getId().longValue(), habitId, completedOn);
        } catch (RuntimeException exception) {
            String sqlState = sqlStateOf(exception);
            if (UNIQUE_VIOLATION.equals(sqlState)) {
                throw new TrackingAlreadyExistsException(habitId, completedOn);
            }
            if (FOREIGN_KEY_VIOLATION.equals(sqlState)) {
                throw new HabitNotExistsException(habitId);
            }
            throw exception;
        }
    }

    private static Tracking toTracking(TrackingsRecord record) {
        return new Tracking(
                record.getId().longValue(),
                record.getHabitId().longValue(),
                record.getCompletedOn()
        );
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
