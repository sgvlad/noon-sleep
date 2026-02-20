package com.noom.interview.fullstack.sleep.sleeplog.control;

public class DuplicateSleepLogException extends RuntimeException {
    public DuplicateSleepLogException(String message, Throwable cause) {
        super(message, cause);
    }
}
