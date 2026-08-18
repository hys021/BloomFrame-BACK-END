package com.bloomframe.server.alarm.model;

import com.google.cloud.firestore.annotation.Exclude;

public class ExerciseAlarm {

    @Exclude
    private String id;

    private String userId;
    private String exerciseName;
    private String alarmTime; // "HH:mm"

    public ExerciseAlarm() {
    }

    public ExerciseAlarm(String userId, String exerciseName, String alarmTime) {
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.alarmTime = alarmTime;
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

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
}
