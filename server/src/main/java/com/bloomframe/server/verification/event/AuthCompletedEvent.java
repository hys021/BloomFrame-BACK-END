package com.bloomframe.server.verification.event;

/**
 * Published after a successful touch auth. Newsletter FCM is handled by the AI listener.
 */
public record AuthCompletedEvent(String uid, String reminderId) {
}
