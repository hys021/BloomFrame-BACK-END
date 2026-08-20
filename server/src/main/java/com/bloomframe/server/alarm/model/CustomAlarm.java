package com.bloomframe.server.alarm.model;

import com.google.cloud.firestore.annotation.Exclude;

public class CustomAlarm {

    @Exclude
    private String id;

    private String userId;
    private String title;
    private String alarmTime; // "HH:mm"
    private String startDate; // "yyyy-MM-dd"

    public CustomAlarm() {
    }

    public CustomAlarm(
            String userId,
            String title,
            String alarmTime,
            String startDate
    ) {
        this.userId = userId;
        this.title = title;
        this.alarmTime = alarmTime;
        this.startDate = startDate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}