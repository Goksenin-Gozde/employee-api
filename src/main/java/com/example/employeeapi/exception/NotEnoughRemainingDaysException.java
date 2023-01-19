package com.example.employeeapi.exception;

public class NotEnoughRemainingDaysException extends RuntimeException {
    public NotEnoughRemainingDaysException(String message) {
        super(message);
    }
}
