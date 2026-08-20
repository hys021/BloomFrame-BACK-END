package com.bloomframe.server.alarm.model;

import com.google.cloud.firestore.annotation.Exclude;

/**
 * Firestore "medicationAlarms" 컬렉션 문서 매핑 클래스.
 * alarmTime은 Firestore가 LocalTime을 기본 지원하지 않아 "HH:mm" 문자열로 저장합니다.
 * (LocalTime <-> String 변환은 Service 계층에서 처리)
 */
public class MedicationAlarm {

    @Exclude
    private String id;

    private String userId;
    private String alarmTime; // "HH:mm"
    private String startDate; // "yyyy-MM-dd"

    public MedicationAlarm() {
    }

    public MedicationAlarm(String userId, String alarmTime, String startDate) {
        this.userId = userId;
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
