package com.noom.interview.fullstack.sleep.sleeplog.boundary;

import com.noom.interview.fullstack.sleep.sleeplog.control.SleepLogService;
import com.noom.interview.fullstack.sleep.sleeplog.entity.SleepLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sleep-log")
public class SleepLogController {

    private final SleepLogService sleepLogService;

    public SleepLogController(SleepLogService sleepLogService) {
        this.sleepLogService = sleepLogService;
    }

    @PostMapping
    public ResponseEntity<SleepLogResponse> createSleepLog(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateSleepLogRequest request) {

        SleepLog sleepLog = sleepLogService.createSleepLog(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SleepLogResponse.fromSleepLog(sleepLog));
    }

    @GetMapping("/last-night")
    public ResponseEntity<SleepLogResponse> getLastNightSleep(
            @RequestHeader("X-User-Id") Long userId) {

        SleepLog sleepLog = sleepLogService.getLastNightSleep(userId);

        return ResponseEntity.ok(SleepLogResponse.fromSleepLog(sleepLog));
    }
}
