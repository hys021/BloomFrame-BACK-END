package com.bloomframe.server.healthcondition.model;

import com.google.cloud.firestore.annotation.Exclude;

/**
 * Firestore "healthConditions" 컬렉션 문서 매핑 클래스.
 * Python AI 서버가 이 컬렉션을 조회해서 뉴스레터/건강 추천 생성에 활용합니다.
 */
public class HealthCondition {

    @Exclude
    private String id;

    private String userId;
    private String conditionName;

    public HealthCondition() {
    }

    public HealthCondition(String userId, String conditionName) {
        this.userId = userId;
        this.conditionName = conditionName;
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

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }
}
