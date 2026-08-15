package com.bloomframe.server.reminder.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.Exclude;

public class Reminder {

    @Exclude
    private String id;

    private String userId;
    private String type;
    private String targetId;
    private Timestamp scheduledAt;
    private String status;

    public Reminder() {
    }

    public Reminder(
            String userId,
            String type,
            String targetId,
            Timestamp scheduledAt,
            String status
    ) {
        this.userId = userId;
        this.type = type;
        this.targetId = targetId;
        this.scheduledAt = scheduledAt;
        this.status = status;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Timestamp getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Timestamp scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}