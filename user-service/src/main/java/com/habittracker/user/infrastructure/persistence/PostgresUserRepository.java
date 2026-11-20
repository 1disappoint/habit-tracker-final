package com.habittracker.user.infrastructure.persistence;

import com.habittracker.user.domain.exception.UserAlreadyExistsException;
import com.habittracker.user.domain.model.User;
import com.habittracker.user.domain.repository.UserRepository;
import com.habittracker.user.infrastructure.persistence.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.habittracker.user.infrastructure.persistence.jooq.tables.Users.USERS;

/**
 * Хранилище пользователей в PostgreSQL (jOOQ).
 * SQL строится типобезопасно на таблицах, сгенерированных из db/init.sql.
 */
@Repository
public class PostgresUserRepository implements UserRepository {

    private static final String UNIQUE_VIOLATION = "23505";

    private final DSLContext dsl;

    public PostgresUserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<User> findAll() {
        return dsl.selectFrom(USERS)
                .orderBy(USERS.ID)
                .fetch(PostgresUserRepository::toUser);
    }

    @Override
    public Optional<User> findById(long id) {
        return dsl.selectFrom(USERS)
                .where(USERS.ID.eq(Math.toIntExact(id)))
                .fetchOptional(PostgresUserRepository::toUser);
    }

    @Override
    public User create(String name, String email) {
        try {
            UsersRecord inserted = dsl.insertInto(USERS)
                    .set(USERS.NAME, name)
                    .set(USERS.EMAIL, email)
                    .returning(USERS.ID)
                    .fetchOne();

            if (inserted == null || inserted.getId() == null) {
                throw new IllegalStateException("PostgreSQL did not return generated id for users");
            }
            return new User(inserted.getId().longValue(), name, email);
        } catch (RuntimeException exception) {
            if (UNIQUE_VIOLATION.equals(sqlStateOf(exception))) {
                throw new UserAlreadyExistsException(email);
            }
            throw exception;
        }
    }

    private static User toUser(UsersRecord record) {
        return new User(record.getId().longValue(), record.getName(), record.getEmail());
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
