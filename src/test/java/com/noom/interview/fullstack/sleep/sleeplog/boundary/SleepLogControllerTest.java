package com.noom.interview.fullstack.sleep.sleeplog.boundary;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noom.interview.fullstack.sleep.sleeplog.control.DuplicateSleepLogException;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepAverages;
import com.noom.interview.fullstack.sleep.sleeplog.control.SleepLogNotFoundException;
import com.noom.interview.fullstack.sleep.sleeplog.control.SleepLogService;
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
class SleepLogControllerTest {

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        @Primary
        public SleepLogService sleepLogService() {
            return Mockito.mock(SleepLogService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SleepLogService sleepLogService;

    private static final Long USER_ID = 1L;
    private static final LocalDateTime BED_TIME = LocalDateTime.of(2026, 2, 19, 23, 30);
    private static final LocalDateTime WAKE_TIME = LocalDateTime.of(2026, 2, 20, 7, 0);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        reset(sleepLogService);
    }

    @Test
    void postSleepLog_validRequest_returns201() throws Exception {
        SleepLog savedLog = new SleepLog(1L, USER_ID, WAKE_TIME.toLocalDate(), BED_TIME, WAKE_TIME, MorningFeeling.GOOD, LocalDateTime.now());
        CreateSleepLogRequest expectedRequest = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.GOOD);
        when(sleepLogService.createSleepLog(eq(USER_ID), eq(expectedRequest)))
                .thenReturn(savedLog);

        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.GOOD);

        mockMvc.perform(post("/api/sleep-log")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sleepDate").value("2026-02-20"))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
                .andExpect(jsonPath("$.totalTimeInBed").value("PT7H30M"));
    }

    @Test
    void postSleepLog_invalidInput_returns400() throws Exception {
        when(sleepLogService.createSleepLog(any(), any()))
                .thenThrow(new IllegalArgumentException("Wake time must be after bed time"));

        CreateSleepLogRequest request = new CreateSleepLogRequest(WAKE_TIME, BED_TIME, MorningFeeling.OK);

        mockMvc.perform(post("/api/sleep-log")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Wake time must be after bed time"));
    }

    @Test
    void postSleepLog_duplicate_returns409() throws Exception {
        when(sleepLogService.createSleepLog(any(), any()))
                .thenThrow(new DuplicateSleepLogException("Sleep log already exists", new RuntimeException()));

        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.GOOD);

        mockMvc.perform(post("/api/sleep-log")
                        .header("X-User-Id", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Sleep log already exists"));
    }

    @Test
    void postSleepLog_missingUserIdHeader_returns400() throws Exception {
        CreateSleepLogRequest request = new CreateSleepLogRequest(BED_TIME, WAKE_TIME, MorningFeeling.GOOD);

        mockMvc.perform(post("/api/sleep-log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLastNightSleep_logExists_returns200() throws Exception {
        SleepLog sleepLog = new SleepLog(1L, USER_ID, WAKE_TIME.toLocalDate(), BED_TIME, WAKE_TIME, MorningFeeling.GOOD, LocalDateTime.now());
        when(sleepLogService.getLastNightSleep(USER_ID)).thenReturn(sleepLog);

        mockMvc.perform(get("/api/sleep-log/last-night")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDate").value("2026-02-20"))
                .andExpect(jsonPath("$.morningFeeling").value("GOOD"))
                .andExpect(jsonPath("$.totalTimeInBed").value("PT7H30M"));
    }

    @Test
    void getLastNightSleep_noLog_returns404() throws Exception {
        when(sleepLogService.getLastNightSleep(USER_ID))
                .thenThrow(new SleepLogNotFoundException("No sleep log found"));

        mockMvc.perform(get("/api/sleep-log/last-night")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No sleep log found"));
    }

    @Test
    void getLast30DayAverages_withData_returns200() throws Exception {
        SleepAverages averages = new SleepAverages(
                LocalDate.of(2026, 1, 22), LocalDate.of(2026, 2, 21),
                Duration.ofHours(8), LocalTime.of(23, 15), LocalTime.of(7, 15),
                Map.of(MorningFeeling.GOOD, 15L, MorningFeeling.OK, 10L, MorningFeeling.BAD, 3L)
        );
        when(sleepLogService.getLast30DayAverages(USER_ID)).thenReturn(averages);

        mockMvc.perform(get("/api/sleep-log/averages")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-01-22"))
                .andExpect(jsonPath("$.to").value("2026-02-21"))
                .andExpect(jsonPath("$.averageTotalTimeInBed").value("PT8H"))
                .andExpect(jsonPath("$.averageBedTime").value("23:15:00"))
                .andExpect(jsonPath("$.averageWakeTime").value("07:15:00"))
                .andExpect(jsonPath("$.morningFeelingFrequencies.GOOD").value(15))
                .andExpect(jsonPath("$.morningFeelingFrequencies.OK").value(10))
                .andExpect(jsonPath("$.morningFeelingFrequencies.BAD").value(3));
    }

    @Test
    void getLast30DayAverages_noData_returns200WithEmptyResponse() throws Exception {
        SleepAverages emptyAverages = new SleepAverages(
                LocalDate.of(2026, 1, 22), LocalDate.of(2026, 2, 21),
                Duration.ZERO, null, null, Map.of()
        );
        when(sleepLogService.getLast30DayAverages(USER_ID)).thenReturn(emptyAverages);

        mockMvc.perform(get("/api/sleep-log/averages")
                        .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-01-22"))
                .andExpect(jsonPath("$.to").value("2026-02-21"))
                .andExpect(jsonPath("$.averageTotalTimeInBed").value("PT0S"))
                .andExpect(jsonPath("$.averageBedTime").isEmpty())
                .andExpect(jsonPath("$.averageWakeTime").isEmpty())
                .andExpect(jsonPath("$.morningFeelingFrequencies").isEmpty());
    }
}
