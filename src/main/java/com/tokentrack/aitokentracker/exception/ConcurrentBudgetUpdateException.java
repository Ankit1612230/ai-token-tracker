package com.tokentrack.aitokentracker.exception;

public class ConcurrentBudgetUpdateException extends RuntimeException {
    public ConcurrentBudgetUpdateException(String message) {
        super(message);
    }
}