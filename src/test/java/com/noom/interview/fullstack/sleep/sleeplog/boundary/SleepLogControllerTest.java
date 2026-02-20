package com.noom.interview.fullstack.sleep.sleeplog.boundary;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noom.interview.fullstack.sleep.sleeplog.control.DuplicateSleepLogException;
import com.noom.interview.fullstack.sleep.sleeplog.control.SleepLogService;
import com.noom.interview.fullstack.sleep.sleeplog.entity.MorningFeeling;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
