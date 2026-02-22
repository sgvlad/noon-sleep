package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SleepLogTest {

    @Test
    void fromRequest_sleepDateDerivedFromWakeTime() {
        CreateSleepLogRequest request = new CreateSleepLogRequest(
                LocalDateTime.of(2026, 5, 14, 23, 0),
                LocalDateTime.of(2026, 5, 15, 7, 0),
                MorningFeeling.GOOD
        );

        SleepLog sleepLog = SleepLog.fromRequest(1L, request);

        assertThat(sleepLog.sleepDate()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    @Test
    void fromRequest_sleepDateMatchesWakeTimeDateNotBedTimeDate() {
        CreateSleepLogRequest request = new CreateSleepLogRequest(
                LocalDateTime.of(2026, 3, 31, 23, 30),
                LocalDateTime.of(2026, 4, 1, 6, 30),
                MorningFeeling.OK
        );

        SleepLog sleepLog = SleepLog.fromRequest(1L, request);

        assertThat(sleepLog.sleepDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(sleepLog.sleepDate()).isNotEqualTo(request.bedTime().toLocalDate());
    }

    @Test
    void constructor_wakeTimeBeforeBedTime_throwsException() {
        assertThatThrownBy(() -> new SleepLog(null, 1L, LocalDate.of(2026, 1, 1),
                LocalDateTime.of(2026, 1, 1, 7, 0),
                LocalDateTime.of(2026, 1, 1, 6, 0),
                MorningFeeling.BAD, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wake time must be after bed time");
    }

    @Test
    void constructor_wakeTimeEqualsBedTime_throwsException() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 1, 1, 7, 0);

        assertThatThrownBy(() -> new SleepLog(null, 1L, LocalDate.of(2026, 1, 1),
                sameTime, sameTime,
                MorningFeeling.OK, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wake time must be after bed time");
    }
}
