package com.noom.interview.fullstack.sleep.sleeplog.boundary;

import java.time.LocalDateTime;

import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;

public record CreateSleepLogRequest(
        LocalDateTime bedTime,
        LocalDateTime wakeTime,
        MorningFeeling morningFeeling
) {}
