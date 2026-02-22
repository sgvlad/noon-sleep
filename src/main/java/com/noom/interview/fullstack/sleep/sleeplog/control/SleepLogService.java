package com.noom.interview.fullstack.sleep.sleeplog.control;

import java.time.LocalDate;
import java.util.List;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepAverages;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SleepLogService {

    private static final int AVERAGES_PERIOD_DAYS = 30;

    private final SleepLogRepository sleepLogRepository;

    public SleepLogService(SleepLogRepository sleepLogRepository) {
        this.sleepLogRepository = sleepLogRepository;
    }

    public SleepLog createSleepLog(Long userId, CreateSleepLogRequest request) {
        SleepLog sleepLog = SleepLog.fromRequest(userId, request);
        return sleepLogRepository.save(sleepLog);
    }

    public SleepLog getLastNightSleep(Long userId) {
        LocalDate today = LocalDate.now();
        return sleepLogRepository.findByUserIdAndDate(userId, today)
                .orElseThrow(() -> new SleepLogNotFoundException("No sleep log found for user " + userId + " on " + today));
    }

    public SleepAverages getLast30DayAverages(Long userId) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(AVERAGES_PERIOD_DAYS);
        List<SleepLog> sleepLogs = sleepLogRepository.findByUserIdAndDateRange(userId, from, to);
        return SleepAverages.fromSleepLogs(sleepLogs, from, to);
    }
}
