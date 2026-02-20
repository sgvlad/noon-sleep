package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        return jdbc.queryForObject(INSERT_SLEEP_LOG, mapToSqlParams(sleepLog), this::mapToSleepLog);
    }

    private MapSqlParameterSource mapToSqlParams(SleepLog sleepLog) {
        return new MapSqlParameterSource()
                .addValue("userId", sleepLog.userId())
                .addValue("sleepDate", sleepLog.sleepDate())
                .addValue("bedTime", sleepLog.bedTime())
                .addValue("wakeTime", sleepLog.wakeTime())
                .addValue("morningFeeling", sleepLog.morningFeeling().name());
    }

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

    private static final String INSERT_SLEEP_LOG = """
            INSERT INTO sleep_log (user_id, sleep_date, bed_time, wake_time, morning_feeling)
            VALUES (:userId, :sleepDate, :bedTime, :wakeTime, :morningFeeling)
            RETURNING *
            """;

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
