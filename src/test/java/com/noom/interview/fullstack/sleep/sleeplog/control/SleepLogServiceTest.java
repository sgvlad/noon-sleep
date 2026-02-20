package com.noom.interview.fullstack.sleep.sleeplog.control;

import java.time.LocalDateTime;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

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
        when(sleepLogRepository.save(any(SleepLog.class))).thenThrow(new DuplicateKeyException("duplicate"));

        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.BAD);
        assertThatThrownBy(() -> sleepLogService.createSleepLog(USER_ID, request))
                .isInstanceOf(DuplicateSleepLogException.class)
                .hasMessageContaining("Sleep log already exists");
    }
}
