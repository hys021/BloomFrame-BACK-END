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
- [팀 구성](#팀-구성)
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

## 👥 팀 구성

6인 팀으로, 백엔드는 3명이 도메인 단위로 나누어 개발했습니다.

| 역할 | 담당 영역 |
|---|---|
| 기획 | 서비스 기획, 화면 설계 |
| Java #1 | 계정/정보관리 — 회원가입, JWT, 페어링, 알림 CRUD |
| **Java #2 (본인)** | **실시간 동기화·인증처리 — 터치 인증, 인증 로그, 식물 상태, 폴링 API** |
| AI/데이터 | 약봉지 사진 분석(OCR), AI 뉴스레터 생성, Firebase 인프라 초기 설정 |
| Frontend | 앱·IoT 화면 개발 |
| Design | UI/UX 디자인 |

## 🌼 담당 영역

**Java #2 — 실시간 동기화 · 인증처리**를 담당했습니다. (`verification`, `flower`, `sync` 패키지)

### 신경 쓴 부분

**1. 터치 인증의 클라이언트 비신뢰 설계**
클라이언트는 `reminderId`만 전달하고, 서버가 해당 리마인더의 예정 시각을 직접 조회해 현재 시각과 비교하여 알림 단계(1차/2차)와 성공 여부를 스스로 판단하도록 설계했습니다. 클라이언트가 알림 단계나 시각 같은 판정 근거를 직접 보내는 구조는 조작 가능성이 있어, 인증 기록의 신뢰성을 서버가 전적으로 통제하도록 했습니다.

**2. 식물 상태 판정 시 "최신 리마인더 우선" 규칙**
식물 상태는 가장 최근 알림 하나에만 반응해야 하는데, 초기 구현은 유저의 모든 리마인더를 동일한 조건으로 순회하면서 이미 인증에 성공한 뒤에도 과거에 놓쳤던 알림이 재조회되어 상태를 다시 시듦으로 덮어쓰는 회귀가 있었습니다. 판정 대상을 "이미 도래한 리마인더 중 가장 최근 것 하나"로 좁혀 해결하고, 동일 조건을 검증하는 회귀 테스트로 고정했습니다.

**3. Firestore 읽기 비용을 고려한 조회 구조 개선**
스케줄러가 전체 유저를 순회하며 매분 리마인더를 조회하는 구조는 유저 수와 실행 횟수에 비례해 Firestore 읽기 요청이 누적되어, 실제로 해커톤 기간 중 일일 읽기 할당량을 초과해 회원가입 등 무관한 기능까지 장애가 발생하는 원인이 되었습니다. 유저 목록을 먼저 조회하지 않고 `collectionGroup` 쿼리로 전체 유저의 리마인더를 한 번에 가져오는 구조로 전환해, 읽기 비용이 유저 수와 무관하게 고정되도록 개선했습니다.

**4. 계층형 패키지 구조와 명확한 소유권 경계**
`verification`, `flower`, `sync` 각 패키지를 `model / repository / service / controller` 계층으로 통일해 구성하고, 다른 팀원 소유의 데이터(`reminders`, `users`)는 읽기 전용 인터페이스로만 접근하도록 제한했습니다. 이를 통해 여러 명이 동시에 개발하면서도 파일 충돌과 소유권 혼선을 최소화했습니다.
