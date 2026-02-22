package com.noom.interview.fullstack.sleep.sleeplog.control;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepAverages;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SleepLogServiceTest {

    @Mock
    private SleepLogRepository sleepLogRepository;

    @InjectMocks
    private SleepLogService sleepLogService;

    private static final Long USER_ID = 1L;
    private static final LocalDateTime BED_TIME = LocalDateTime.of(2026, 2, 19, 23, 30);
    private static final LocalDateTime WAKE_TIME = LocalDateTime.of(2026, 2, 20, 7, 0);

    @Test
    void createSleepLog_validInput_returnsSavedLog() {
        SleepLog savedLog = new SleepLog(1L, USER_ID, WAKE_TIME.toLocalDate(), BED_TIME, WAKE_TIME, MorningFeeling.GOOD, LocalDateTime.now());
        when(sleepLogRepository.save(any(SleepLog.class))).thenReturn(savedLog);

        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.GOOD);
        SleepLog result = sleepLogService.createSleepLog(USER_ID, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.morningFeeling()).isEqualTo(MorningFeeling.GOOD);
    }

    @Test
    void createSleepLog_wakeTimeBeforeBedTime_throwsIllegalArgument() {
        CreateSleepLogRequest request = new CreateSleepLogRequest(WAKE_TIME, BED_TIME, MorningFeeling.OK);
        assertThatThrownBy(() -> sleepLogService.createSleepLog(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wake time must be after bed time");
    }

    @Test
    void createSleepLog_wakeTimeEqualsBedTime_throwsIllegalArgument() {
        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, BED_TIME, MorningFeeling.OK);
        assertThatThrownBy(() -> sleepLogService.createSleepLog(USER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wake time must be after bed time");
    }

    @Test
    void createSleepLog_duplicateEntry_throwsDuplicateSleepLogException() {
        when(sleepLogRepository.save(any(SleepLog.class))).thenThrow(new DuplicateSleepLogException("duplicate", new RuntimeException()));

        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.BAD);
        assertThatThrownBy(() -> sleepLogService.createSleepLog(USER_ID, request))
                .isInstanceOf(DuplicateSleepLogException.class);
    }

    @Test
    void getLastNightSleep_logExists_returnsLog() {
        LocalDate today = LocalDate.now();
        SleepLog existingLog = new SleepLog(1L, USER_ID, today, BED_TIME, WAKE_TIME, MorningFeeling.GOOD, LocalDateTime.now());
        when(sleepLogRepository.findByUserIdAndDate(USER_ID, today)).thenReturn(Optional.of(existingLog));

        SleepLog result = sleepLogService.getLastNightSleep(USER_ID);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.sleepDate()).isEqualTo(today);
    }

    @Test
    void getLastNightSleep_noLog_throwsSleepLogNotFoundException() {
        LocalDate today = LocalDate.now();
        when(sleepLogRepository.findByUserIdAndDate(USER_ID, today)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sleepLogService.getLastNightSleep(USER_ID))
                .isInstanceOf(SleepLogNotFoundException.class)
                .hasMessageContaining("No sleep log found");
    }

    @Test
    void getLast30DayAverages_withLogs_returnsCorrectAverages() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, USER_ID, today.minusDays(2),
                        LocalDateTime.of(2026, 2, 18, 23, 0), LocalDateTime.of(2026, 2, 19, 7, 0), MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(2L, USER_ID, today.minusDays(1),
                        LocalDateTime.of(2026, 2, 19, 23, 30), LocalDateTime.of(2026, 2, 20, 7, 30), MorningFeeling.OK, LocalDateTime.now())
        );
        when(sleepLogRepository.findByUserIdAndDateRange(USER_ID, from, today)).thenReturn(sleepLogs);

        SleepAverages result = sleepLogService.getLast30DayAverages(USER_ID);

        assertThat(result.from()).isEqualTo(from);
        assertThat(result.to()).isEqualTo(today);
        assertThat(result.averageTotalTimeInBed()).isEqualTo(Duration.ofHours(8));
        assertThat(result.averageBedTime()).isEqualTo(LocalTime.of(23, 15));
        assertThat(result.averageWakeTime()).isEqualTo(LocalTime.of(7, 15));
        assertThat(result.morningFeelingFrequencies()).containsEntry(MorningFeeling.GOOD, 1L);
        assertThat(result.morningFeelingFrequencies()).containsEntry(MorningFeeling.OK, 1L);
    }

    @Test
    void getLast30DayAverages_morningFeelingFrequencies_countsCorrectly() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);
        List<SleepLog> sleepLogs = List.of(
                new SleepLog(1L, USER_ID, today.minusDays(3), BED_TIME, WAKE_TIME, MorningFeeling.BAD, LocalDateTime.now()),
                new SleepLog(2L, USER_ID, today.minusDays(2), BED_TIME, WAKE_TIME, MorningFeeling.GOOD, LocalDateTime.now()),
                new SleepLog(3L, USER_ID, today.minusDays(1), BED_TIME, WAKE_TIME, MorningFeeling.GOOD, LocalDateTime.now())
        );
        when(sleepLogRepository.findByUserIdAndDateRange(USER_ID, from, today)).thenReturn(sleepLogs);

        SleepAverages result = sleepLogService.getLast30DayAverages(USER_ID);

        assertThat(result.morningFeelingFrequencies()).containsEntry(MorningFeeling.BAD, 1L);
        assertThat(result.morningFeelingFrequencies()).containsEntry(MorningFeeling.GOOD, 2L);
        assertThat(result.morningFeelingFrequencies()).doesNotContainKey(MorningFeeling.OK);
    }

    @Test
    void getLast30DayAverages_noLogs_returnsEmptyResponse() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(30);
        when(sleepLogRepository.findByUserIdAndDateRange(USER_ID, from, today)).thenReturn(List.of());

        SleepAverages result = sleepLogService.getLast30DayAverages(USER_ID);

        assertThat(result.from()).isEqualTo(from);
        assertThat(result.to()).isEqualTo(today);
        assertThat(result.averageTotalTimeInBed()).isEqualTo(Duration.ZERO);
        assertThat(result.averageBedTime()).isNull();
        assertThat(result.averageWakeTime()).isNull();
        assertThat(result.morningFeelingFrequencies()).isEmpty();
    }
}
