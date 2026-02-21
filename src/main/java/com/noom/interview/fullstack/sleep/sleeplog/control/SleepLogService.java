package com.noom.interview.fullstack.sleep.sleeplog.control;

import java.time.LocalDate;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogRepository;
import org.springframework.stereotype.Service;

@Service
public class SleepLogService {

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
}
