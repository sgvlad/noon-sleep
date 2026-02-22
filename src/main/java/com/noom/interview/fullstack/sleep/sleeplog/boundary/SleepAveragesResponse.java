package com.noom.interview.fullstack.sleep.sleeplog.boundary;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepAverages;
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;

public record SleepAveragesResponse(
        LocalDate from,
        LocalDate to,
        Duration averageTotalTimeInBed,
        LocalTime averageBedTime,
        LocalTime averageWakeTime,
        Map<MorningFeeling, Long> morningFeelingFrequencies
) {
    public static SleepAveragesResponse fromSleepAverages(SleepAverages averages) {
        return new SleepAveragesResponse(
                averages.from(),
                averages.to(),
                averages.averageTotalTimeInBed(),
                averages.averageBedTime(),
                averages.averageWakeTime(),
                averages.morningFeelingFrequencies()
        );
    }
}
