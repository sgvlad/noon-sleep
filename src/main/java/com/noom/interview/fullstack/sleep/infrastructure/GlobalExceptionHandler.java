package com.noom.interview.fullstack.sleep.infrastructure;

import java.util.Map;

import com.noom.interview.fullstack.sleep.sleeplog.control.DuplicateSleepLogException;
import com.noom.interview.fullstack.sleep.sleeplog.control.SleepLogNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateSleepLogException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateSleepLog(DuplicateSleepLogException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(SleepLogNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSleepLogNotFound(SleepLogNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }
}
