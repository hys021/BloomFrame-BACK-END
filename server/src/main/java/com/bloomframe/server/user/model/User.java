package com.bloomframe.server.user.model;

import com.google.cloud.firestore.annotation.Exclude;

/**
 * Firestore "users" 컬렉션 문서 매핑 클래스.
 * Firestore SDK가 기본 생성자 + getter/setter(또는 public 필드)로 자동 매핑합니다.
 * 문서 ID(id) 자체는 문서 데이터에 저장되지 않으므로 @Exclude 처리합니다.
 */
public class User {

    @Exclude
    private String id; // Firestore document id (자동 생성)

    private String name;
    private String caregiverPhone;
    private String selfPhone;
    private String email;
    private String password;

    public User() {
        // Firestore 역직렬화를 위한 기본 생성자 필수
    }

    public User(String name, String caregiverPhone, String selfPhone, String email, String password) {
        this.name = name;
        this.caregiverPhone = caregiverPhone;
        this.selfPhone = selfPhone;
        this.email = email;
        this.password = password;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaregiverPhone() {
        return caregiverPhone;
    }

    public void setCaregiverPhone(String caregiverPhone) {
        this.caregiverPhone = caregiverPhone;
    }

    public String getSelfPhone() {
        return selfPhone;
    }

    public void setSelfPhone(String selfPhone) {
        this.selfPhone = selfPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
