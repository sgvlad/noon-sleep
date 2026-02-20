package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;

public record SleepLog(
        Long id,
        Long userId,
        LocalDate sleepDate,
        LocalDateTime bedTime,
        LocalDateTime wakeTime,
        MorningFeeling morningFeeling,
        LocalDateTime createdAt
) {
    public SleepLog {
        if (bedTime != null && wakeTime != null && !wakeTime.isAfter(bedTime)) {
            throw new IllegalArgumentException("Wake time must be after bed time");
        }
    }

    public static SleepLog fromRequest(Long userId, CreateSleepLogRequest request) {
        return new SleepLog(null, userId, LocalDate.now(), request.bedTime(), request.wakeTime(), request.morningFeeling(), null);
    }
}
