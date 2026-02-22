package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.noom.interview.fullstack.sleep.sleeplog.control.DuplicateSleepLogException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SleepLogRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SleepLogRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SleepLog save(SleepLog sleepLog) {
        try {
            return jdbc.queryForObject(INSERT_SLEEP_LOG, mapToInsertParams(sleepLog), this::mapToSleepLog);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateSleepLogException(
                    "Sleep log already exists for user " + sleepLog.userId() + " on " + sleepLog.sleepDate(), exception);
        }
    }

    public Optional<SleepLog> findByUserIdAndDate(Long userId, LocalDate sleepDate) {
        try {
            SleepLog sleepLog = jdbc.queryForObject(FIND_BY_USER_ID_AND_DATE, mapToFindByUserParams(userId, sleepDate), this::mapToSleepLog);
            return Optional.ofNullable(sleepLog);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<SleepLog> findByUserIdAndDateRange(Long userId, LocalDate from, LocalDate to) {
        return jdbc.query(FIND_BY_USER_ID_AND_DATE_RANGE, mapToFindByUserDateRangeParams(userId, from, to), this::mapToSleepLog);
    }

    private MapSqlParameterSource mapToInsertParams(SleepLog sleepLog) {
        return new MapSqlParameterSource()
                .addValue("userId", sleepLog.userId())
                .addValue("sleepDate", sleepLog.sleepDate())
                .addValue("bedTime", sleepLog.bedTime())
                .addValue("wakeTime", sleepLog.wakeTime())
                .addValue("morningFeeling", sleepLog.morningFeeling().name());
    }

    private MapSqlParameterSource mapToFindByUserParams(Long userId, LocalDate sleepDate) {
        return new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("sleepDate", sleepDate);
    }

    private MapSqlParameterSource mapToFindByUserDateRangeParams(Long userId, LocalDate from, LocalDate to) {
        return new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("from", from)
                .addValue("to", to);
    }

    private static final String INSERT_SLEEP_LOG = """
            INSERT INTO sleep_log (user_id, sleep_date, bed_time, wake_time, morning_feeling)
            VALUES (:userId, :sleepDate, :bedTime, :wakeTime, :morningFeeling)
            RETURNING *
            """;

    private static final String FIND_BY_USER_ID_AND_DATE = """
            SELECT * FROM sleep_log
            WHERE user_id = :userId AND sleep_date = :sleepDate
            """;

    private static final String FIND_BY_USER_ID_AND_DATE_RANGE = """
            SELECT * FROM sleep_log
            WHERE user_id = :userId AND sleep_date > :from AND sleep_date <= :to
            """;

    private SleepLog mapToSleepLog(ResultSet rs, int rowNum) throws SQLException {
        return new SleepLog(
                rs.getLong(Column.ID),
                rs.getLong(Column.USER_ID),
                rs.getDate(Column.SLEEP_DATE).toLocalDate(),
                rs.getTimestamp(Column.BED_TIME).toLocalDateTime(),
                rs.getTimestamp(Column.WAKE_TIME).toLocalDateTime(),
                MorningFeeling.valueOf(rs.getString(Column.MORNING_FEELING)),
                rs.getTimestamp(Column.CREATED_AT).toLocalDateTime()
        );
    }
    private static class Column {
        static final String ID = "id";
        static final String USER_ID = "user_id";
        static final String SLEEP_DATE = "sleep_date";
        static final String BED_TIME = "bed_time";
        static final String WAKE_TIME = "wake_time";
        static final String MORNING_FEELING = "morning_feeling";
        static final String CREATED_AT = "created_at";
    }
}
