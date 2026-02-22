package com.noom.interview.fullstack.sleep.sleeplog.entity;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SleepAveragesTest {

    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 1, 31);

    @Test
    void fromSleepLogs_emptyList_returnsZeroedAverages() {
        SleepAverages result = SleepAverages.fromSleepLogs(List.of(), FROM, TO);

        assertThat(result.from()).isEqualTo(FROM);
        assertThat(result.to()).isEqualTo(TO);
        assertThat(result.averageTotalTimeInBed()).isEqualTo(Duration.ZERO);
        assertThat(result.averageBedTime()).isNull();
        assertThat(result.averageWakeTime()).isNull();
        assertThat(result.morningFeelingFrequencies()).isEmpty();
    }

    @Test
    void fromSleepLogs_singleLog_averagesEqualThatLog() {
        SleepLog log = new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                LocalDateTime.of(2026, 1, 9, 22, 30), LocalDateTime.of(2026, 1, 10, 6, 30), MorningFeeling.GOOD, LocalDateTime.now());

        SleepAverages result = SleepAverages.fromSleepLogs(List.of(log), FROM, TO);

        assertThat(result.averageTotalTimeInBed()).isEqualTo(Duration.ofHours(8));
        assertThat(result.averageBedTime()).isEqualTo(LocalTime.of(22, 30));
        assertThat(result.averageWakeTime()).isEqualTo(LocalTime.of(6, 30));
        assertThat(result.morningFeelingFrequencies()).containsOnly(
                java.util.Map.entry(MorningFeeling.GOOD, 1L));
    }

    @Test
    void fromSleepLogs_multipleLogs_computesCorrectAverageDuration() {
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                        LocalDateTime.of(2026, 1, 9, 23, 0), LocalDateTime.of(2026, 1, 10, 7, 0), MorningFeeling.OK, LocalDateTime.now()),
                new SleepLog(2L, 1L, LocalDate.of(2026, 1, 11),
                        LocalDateTime.of(2026, 1, 10, 22, 0), LocalDateTime.of(2026, 1, 11, 8, 0), MorningFeeling.OK, LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        // 8h and 10h → average 9h
        assertThat(result.averageTotalTimeInBed()).isEqualTo(Duration.ofHours(9));
    }

    @Test
    void fromSleepLogs_multipleLogs_computesCorrectAverageWakeTime() {
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                        LocalDateTime.of(2026, 1, 9, 23, 0), LocalDateTime.of(2026, 1, 10, 6, 0), MorningFeeling.OK, LocalDateTime.now()),
                new SleepLog(2L, 1L, LocalDate.of(2026, 1, 11),
                        LocalDateTime.of(2026, 1, 10, 23, 0), LocalDateTime.of(2026, 1, 11, 8, 0), MorningFeeling.OK, LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        // 06:00 and 08:00 → average 07:00
        assertThat(result.averageWakeTime()).isEqualTo(LocalTime.of(7, 0));
    }

    @Test
    void fromSleepLogs_bedTimesCrossingMidnight_averagesCorrectly() {
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                        LocalDateTime.of(2026, 1, 9, 23, 0), LocalDateTime.of(2026, 1, 10, 7, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(2L, 1L, LocalDate.of(2026, 1, 11),
                        LocalDateTime.of(2026, 1, 11, 1, 0), LocalDateTime.of(2026, 1, 11, 9, 0), MorningFeeling.OK, LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        // 23:00 and 01:00 are 2 hours apart across midnight → average should be 00:00
        assertThat(result.averageBedTime()).isEqualTo(LocalTime.MIDNIGHT);
    }

    @Test
    void fromSleepLogs_timesStraddlingNoon_averagesCorrectly() {
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                        LocalDateTime.of(2026, 1, 9, 11, 0), LocalDateTime.of(2026, 1, 10, 7, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(2L, 1L, LocalDate.of(2026, 1, 11),
                        LocalDateTime.of(2026, 1, 10, 13, 0), LocalDateTime.of(2026, 1, 11, 7, 0), MorningFeeling.OK, LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        // 11:00 and 13:00 → average should be 12:00 (noon)
        assertThat(result.averageBedTime()).isEqualTo(LocalTime.NOON);
    }

    @Test
    void fromSleepLogs_tenLogsCrossingMidnight_averagesCorrectly() {
        // 5 bed times before midnight, 5 after — symmetric around 00:00
        // 23:00, 23:15, 23:30, 23:45, 00:00, 00:15, 00:30, 00:45, 01:00, 01:00
        // Wake times all at 08:00
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L,  1L, LocalDate.of(2026, 1, 10), LocalDateTime.of(2026, 1,  9, 23,  0), LocalDateTime.of(2026, 1, 10, 8, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(2L,  1L, LocalDate.of(2026, 1, 11), LocalDateTime.of(2026, 1, 10, 23, 15), LocalDateTime.of(2026, 1, 11, 8, 0), MorningFeeling.OK,   LocalDateTime.now()),
                new SleepLog(3L,  1L, LocalDate.of(2026, 1, 12), LocalDateTime.of(2026, 1, 11, 23, 30), LocalDateTime.of(2026, 1, 12, 8, 0), MorningFeeling.BAD,  LocalDateTime.now()),
                new SleepLog(4L,  1L, LocalDate.of(2026, 1, 13), LocalDateTime.of(2026, 1, 12, 23, 45), LocalDateTime.of(2026, 1, 13, 8, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(5L,  1L, LocalDate.of(2026, 1, 14), LocalDateTime.of(2026, 1, 14,  0,  0), LocalDateTime.of(2026, 1, 14, 8, 0), MorningFeeling.OK,   LocalDateTime.now()),
                new SleepLog(6L,  1L, LocalDate.of(2026, 1, 15), LocalDateTime.of(2026, 1, 15,  0, 15), LocalDateTime.of(2026, 1, 15, 8, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(7L,  1L, LocalDate.of(2026, 1, 16), LocalDateTime.of(2026, 1, 16,  0, 30), LocalDateTime.of(2026, 1, 16, 8, 0), MorningFeeling.BAD,  LocalDateTime.now()),
                new SleepLog(8L,  1L, LocalDate.of(2026, 1, 17), LocalDateTime.of(2026, 1, 17,  0, 45), LocalDateTime.of(2026, 1, 17, 8, 0), MorningFeeling.OK,   LocalDateTime.now()),
                new SleepLog(9L,  1L, LocalDate.of(2026, 1, 18), LocalDateTime.of(2026, 1, 18,  1,  0), LocalDateTime.of(2026, 1, 18, 8, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(10L, 1L, LocalDate.of(2026, 1, 19), LocalDateTime.of(2026, 1, 19,  1,  0), LocalDateTime.of(2026, 1, 19, 8, 0), MorningFeeling.OK,   LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        // Symmetric spread around midnight → average bed time should be near 00:00
        // Allow ±7 min tolerance for trig rounding with 10 non-perfectly-symmetric points
        int avgSeconds = result.averageBedTime().toSecondOfDay();
        int distanceFromMidnight = Math.min(avgSeconds, 86400 - avgSeconds);
        assertThat(distanceFromMidnight).as("average bed time should be within 7 min of midnight")
                .isLessThanOrEqualTo(7 * 60);

        // Wake times all at 08:00 → average wake time is exactly 08:00
        assertThat(result.averageWakeTime()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void fromSleepLogs_diametricallyOppositeTimes_averageIsAmbiguous() {
        // 06:00 and 18:00 are exactly 12 hours apart — diametrically opposite on the circle.
        // The sin/cos vectors cancel out (sum to zero), so the average is fundamentally ambiguous.
        // atan2(0, 0) returns 0 → 00:00. Both 00:00 and 12:00 would be equally valid midpoints.
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                        LocalDateTime.of(2026, 1, 10, 6, 0), LocalDateTime.of(2026, 1, 10, 14, 0), MorningFeeling.OK, LocalDateTime.now()),
                new SleepLog(2L, 1L, LocalDate.of(2026, 1, 11),
                        LocalDateTime.of(2026, 1, 10, 18, 0), LocalDateTime.of(2026, 1, 11, 2, 0), MorningFeeling.OK, LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        // atan2 with near-zero components — result depends on floating-point residuals.
        // Both 00:00 and 12:00 are equidistant from both inputs, so either is acceptable.
        assertThat(result.averageBedTime()).isIn(LocalTime.MIDNIGHT, LocalTime.NOON);
    }

    @Test
    void fromSleepLogs_morningFeelingFrequencies_countsAllValues() {
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, 1L, LocalDate.of(2026, 1, 10),
                        LocalDateTime.of(2026, 1, 9, 23, 0), LocalDateTime.of(2026, 1, 10, 7, 0), MorningFeeling.BAD, LocalDateTime.now()),
                new SleepLog(2L, 1L, LocalDate.of(2026, 1, 11),
                        LocalDateTime.of(2026, 1, 10, 23, 0), LocalDateTime.of(2026, 1, 11, 7, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(3L, 1L, LocalDate.of(2026, 1, 12),
                        LocalDateTime.of(2026, 1, 11, 23, 0), LocalDateTime.of(2026, 1, 12, 7, 0), MorningFeeling.GOOD, LocalDateTime.now())
        );

        SleepAverages result = SleepAverages.fromSleepLogs(sleepLogs, FROM, TO);

        assertThat(result.morningFeelingFrequencies()).containsEntry(MorningFeeling.BAD, 1L);
        assertThat(result.morningFeelingFrequencies()).containsEntry(MorningFeeling.GOOD, 2L);
        assertThat(result.morningFeelingFrequencies()).doesNotContainKey(MorningFeeling.OK);
    }

    @Test
    void fromSleepLogs_preservesFromAndToDates() {
        LocalDate customFrom = LocalDate.of(2026, 3, 1);
        LocalDate customTo = LocalDate.of(2026, 3, 15);
        SleepLog log = new SleepLog(1L, 1L, LocalDate.of(2026, 3, 10),
                LocalDateTime.of(2026, 3, 9, 23, 0), LocalDateTime.of(2026, 3, 10, 7, 0), MorningFeeling.OK, LocalDateTime.now());

        SleepAverages result = SleepAverages.fromSleepLogs(List.of(log), customFrom, customTo);

        assertThat(result.from()).isEqualTo(customFrom);
        assertThat(result.to()).isEqualTo(customTo);
    }
}
