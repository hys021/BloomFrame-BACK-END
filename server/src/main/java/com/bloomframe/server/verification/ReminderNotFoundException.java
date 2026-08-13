package com.bloomframe.server.verification;

public class ReminderNotFoundException extends RuntimeException {
    public ReminderNotFoundException(String reminderId) {
        super("reminder not found: " + reminderId);
    }
}