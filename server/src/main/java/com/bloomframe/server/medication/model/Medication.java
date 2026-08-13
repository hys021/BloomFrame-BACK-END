package com.bloomframe.server.medication.model;

import com.google.cloud.firestore.annotation.Exclude;

/**
 * Firestore "medications" 컬렉션 문서 매핑 클래스.
 * devices와 달리 유니크 제약이 필요 없어(한 사용자가 같은 약을 여러 개 등록해도 무방),
 * 문서 ID는 Firestore가 자동 생성합니다.
 */
public class Medication {

    @Exclude
    private String id; // Firestore document id (자동 생성)

    private String userId;
    private String name;
    private Integer dosePerDay;
    private String timing;     // 식전 / 식후 / 취침 전
    private String imageUrl;   // OCR에 사용된 원본 이미지 경로 (R2), null 허용

    public Medication() {
    }

    public Medication(String userId, String name, Integer dosePerDay, String timing, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.dosePerDay = dosePerDay;
        this.timing = timing;
        this.imageUrl = imageUrl;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDosePerDay() {
        return dosePerDay;
    }

    public void setDosePerDay(Integer dosePerDay) {
        this.dosePerDay = dosePerDay;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
