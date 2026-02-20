package com.noom.interview.fullstack.sleep.sleeplog.boundary;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;

public record SleepLogResponse(
        LocalDate sleepDate,
        LocalDateTime bedTime,
        LocalDateTime wakeTime,
        Duration totalTimeInBed,
        MorningFeeling morningFeeling
) {
    public SleepLogResponse(LocalDate sleepDate, LocalDateTime bedTime, LocalDateTime wakeTime, MorningFeeling morningFeeling) {
        this(sleepDate, bedTime, wakeTime, Duration.between(bedTime, wakeTime), morningFeeling);
    }

    public static SleepLogResponse fromSleepLog(SleepLog sleepLog) {
        return new SleepLogResponse(
                sleepLog.sleepDate(),
                sleepLog.bedTime(),
                sleepLog.wakeTime(),
                sleepLog.morningFeeling()
        );
    }
}
