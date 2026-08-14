package com.bloomframe.server.verification.exception;

public class ReminderNotFoundException extends RuntimeException {
    public ReminderNotFoundException(String reminderId) {
        super("reminder not found: " + reminderId);
    }
}