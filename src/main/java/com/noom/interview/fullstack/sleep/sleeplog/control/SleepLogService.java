package com.noom.interview.fullstack.sleep.sleeplog.control;

import com.noom.interview.fullstack.sleep.sleeplog.boundary.CreateSleepLogRequest;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLogRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class SleepLogService {

    private final SleepLogRepository sleepLogRepository;

    public SleepLogService(SleepLogRepository sleepLogRepository) {
        this.sleepLogRepository = sleepLogRepository;
    }

    public SleepLog createSleepLog(Long userId, CreateSleepLogRequest request) {
        SleepLog sleepLog = SleepLog.fromRequest(userId, request);

        try {
            return sleepLogRepository.save(sleepLog);
        } catch (DuplicateKeyException exception) {
            throw new DuplicateSleepLogException("Sleep log already exists for user " + userId + " on " + sleepLog.sleepDate(), exception);
        }
    }
}
