package com.noom.interview.fullstack.sleep.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SleepLog(
        Long id,
        Long userId,
        LocalDate sleepDate,
        LocalDateTime bedTime,
        LocalDateTime wakeTime,
        MorningFeeling morningFeeling,
        LocalDateTime createdAt
) {}
