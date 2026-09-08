# 🌸 BloomFrame Backend

**BloomFrame**은 복약·운동 시간을 자주 놓치는 6070 어르신을 위한 서비스입니다. 자녀가 앱에서 알림을 등록하면, 부모님 댁의 IoT 액자가 알림을 표시하고, 액자를 터치하는 것만으로 복약/운동 인증이 완료됩니다. 인증 여부에 따라 액자 속 식물이 피어나거나 시들면서, 부모님의 생활 리듬을 은유적으로 보여줍니다.

이 저장소는 BloomFrame의 **백엔드(Spring Boot) 전체**를 담고 있습니다.

> 멋쟁이사자처럼 중앙해커톤 Animal League 3rd — AAC(초개인화 웰니스) 트랙 · **트랙 3위 수상**
---
**배포 링크**
- 📱 앱: https://bloomframe.vercel.app/
- 📺 IoT 액자: https://bloomframe.vercel.app/display/1052082101 (태블릿에서 실행해야 화면 비율 맞음)

> ⚠️ 해커톤 기간 이후 백엔드 서버는 상시 운영하지 않아, 현재 위 링크에서 로그인/인증 등 실제 기능은 동작하지 않을 수 있습니다.
---

## 목차

- [아키텍처 개요](#아키텍처-개요)
- [핵심 시나리오](#핵심-시나리오)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [API 요약](#api-요약)
- [실행 방법](#실행-방법)
- [담당 영역](#담당-영역)

---

## 🏗️ 아키텍처 개요

```
[자녀 앱]  ──┐
             ├── REST API ──▶ [Spring Boot Server] ──▶ [Firestore]
[IoT 액자]  ──┘                       │
                                       ├──▶ [Cloudflare R2]  (약봉지 이미지)
                                       ├──▶ [FCM]            (뉴스레터 푸시)
                                       └──▶ [Gemini API]     (약봉지 OCR / 뉴스레터 생성)
```

- 별도 실시간 인프라(WebSocket 등) 없이, 클라이언트가 **REST API를 주기적으로 폴링**하는 방식으로 두 기기(앱·액자) 간 상태를 맞춥니다.
- 인증 데이터베이스는 **Firestore(NoSQL)** 단일 사용 — 관계형 DB 없이 사용자 하위 컬렉션 구조로 설계했습니다.

---

## 🌷 핵심 시나리오

1. 자녀가 앱에서 복약/운동/기타 알림을 등록합니다.
2. 등록된 알림 시각이 되면 서버가 자동으로 해당 회차의 리마인더(`reminder`)를 생성합니다.
3. IoT 액자가 리마인더를 표시하고, 부모님이 터치하면 서버가 **직접 시각을 계산**해 1차(3분 이내)/2차(3~10분) 인증 여부를 판정합니다.
4. 인증에 성공하면 액자 속 식물이 피어나고(BLOOMING), 3분 안에 인증이 없으면 시들기 시작하며(WILTED), 10분이 지나면 최종 미인증(MISSED)으로 기록됩니다.
5. 인증 직후에는 사용자의 건강 정보를 반영한 AI 뉴스레터가 생성되어 액자에 표시됩니다.

---

## 🛠️ 기술 스택

| 영역 | 사용 기술 |
|---|---|
| Language / Framework | Java 17, Spring Boot 4.1 |
| Database | Firebase Firestore |
| Auth | Spring Security, JWT (jjwt) |
| File Storage | Cloudflare R2 (S3 호환) |
| Push / AI | Firebase Cloud Messaging, Google Gemini API |
| Build | Gradle |

---

## 🌱 프로젝트 구조

패키지를 **기능(도메인) 단위**로 나누고, 각 패키지 내부는 다시 `controller / service / repository / model` 계층으로 구성했습니다.

```
com.bloomframe.server
├── user/            회원가입, 로그인, JWT, 마이페이지
├── device/          IoT 액자 등록 및 연결(페어링) 관리
├── alarm/           복약·운동·기타 알림(알람) CRUD
├── reminder/        알람 시각 도래 시 리마인더 생성, Wilt/MISSED 판정 스케줄링
├── medication/       복용 약 정보 관리
├── healthcondition/ 지병·알레르기 등 건강 정보 관리
├── verification/    터치 인증 처리, 인증 로그 저장·조회   
├── flower/          식물 상태(BLOOMING/WILTED) 관리            
├── sync/            앱·액자 폴링용 상태 통합 조회 API          
├── ai/              약봉지 사진 분석(OCR), AI 뉴스레터 생성
├── common/          JWT 필터, 공통 예외 처리
├── firebase/        Firebase 초기화, Firestore 연결
├── storage/         Cloudflare R2 파일 업로드
└── config/          Security, CORS, Firebase, Web 설정
```

---

## 📮 API 요약

전체 API는 `/api/v1` 하위에 위치하며, 로그인 이후 발급되는 JWT를 `Authorization: Bearer {token}` 헤더로 전달해야 합니다.

| 분류 | Method | Path | 설명 |
|---|---|---|---|
| 인증 | POST | `/auth/send-code` | 휴대폰 인증번호 발송 |
| 인증 | POST | `/auth/verify-code` | 인증번호 확인 |
| 인증 | POST | `/auth/signup` | 회원가입 |
| 인증 | POST | `/auth/login` | 로그인, JWT 발급 |
| 사용자 | GET/PATCH | `/users/me` | 내 정보 조회/수정 |
| 기기 | POST/GET/PATCH/DELETE | `/devices` | IoT 액자 등록·조회·이름 변경·연결·해제·삭제 |
| 알림 | CRUD | `/medication-alarms`, `/exercise-alarms`, `/custom-alarms` | 복약/운동/기타 알림 관리 |
| 리마인더 | GET | `/reminders/{id}` | 발생한 리마인더 단건 조회 |
| 복용약 | CRUD | `/medications` | 복용 약 정보 관리 |
| 건강정보 | CRUD | `/health-conditions` | 지병·알레르기 등 관리 |
| 약봉지 분석 | POST | `/medications/{id}/analyze` | 약봉지 사진 OCR 분석 |
| 뉴스레터 | GET/POST | `/users/{uid}/newsletters` | 뉴스레터 조회/생성/발송 |
| 인증 처리 | POST | `/auth-touch` | 터치 인증 등록 — 서버가 시각을 직접 계산해 판정 |
| 인증 로그 | GET | `/users/{uid}/auth-logs` | 인증 기록 조회 (기간 필터) |
| 식물 상태 | GET | `/users/{uid}/flower` | 현재 BLOOMING/WILTED 상태 조회 |
| 통합 조회 | GET | `/users/{uid}/sync` | 꽃 상태 + 최근 인증 로그 통합 폴링용 API |

---

## 🚀 실행 방법

```bash
git clone https://github.com/hys021/BloomFrame-BACK-END.git
cd BloomFrame-BACK-END/server
```

**설정 파일 준비** (git에는 포함되어 있지 않음)
1. `application-local.yml.example` → `application-local.yml` 복사 후 아래 값 입력
   - `firebase.credentials-path`, `jwt.secret`, `jwt.expiration-ms`, `r2.*`, `ai.gemini.api-key`
2. `config/firebase-service-account.json` 배치

**실행**
```bash
./gradlew bootRun
```

**헬스체크**
```bash
curl http://localhost:8080/api/v1/firebase/health
# {"firebase":"ok","storage":"r2"}
```

---
