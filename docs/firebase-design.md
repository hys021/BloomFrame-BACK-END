# AAC Firebase 연동 설계

> **담당:** 설계 완료 · **이어서:** Backend A + Backend B (본인 불필요)  
> **온보딩:** [TEAM.md](./TEAM.md) · **할 일:** [TASKS.md](./TASKS.md)

## 1. 목적

- Android/PC 클라이언트의 **회원·알림 설정** 영속 저장
- 약 사진 **Storage** 업로드 (2단계 analyze API 입력)
- IoT 알림·뉴스레터 **FCM** push (2단계)

Spring Boot(`apps/server`)가 **Firebase Admin SDK** 단일 진입점.  
클라이언트는 Firebase Client SDK로 직접 Firestore 쓰기 **하지 않음** (보안·검증은 서버 경유).

---

## 2. Firebase 프로젝트 (Java #2 작업)

| 항목 | 값 (예시) |
|------|-----------|
| 프로젝트 ID | `aac-hackathon` |
| Storage bucket | `aac-hackathon.appspot.com` |
| 리전 | `asia-northeast3` (서울) 권장 |

### Console에서 활성화

1. Authentication — (선택) Email 또는 Anonymous (헤커톤은 `uid` 문자열로 시작 가능)
2. Cloud Firestore — Native mode
3. Cloud Storage
4. Cloud Messaging (FCM)

### 서비스 계정

1. IAM → 서비스 계정 → 새 키 (JSON)
2. 로컬: `apps/server/config/firebase-service-account.json` (gitignore)
3. env: `FIREBASE_CREDENTIALS_PATH=./config/firebase-service-account.json`

---

## 3. Firestore 스키마

### Collection: `users/{uid}`

```json
{
  "userType": "self | representative",
  "name": "홍길동",
  "phone": "010-0000-0000",
  "birthDate": "1950-01-01",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### Subcollection: `users/{uid}/reminders/{reminderId}`

```json
{
  "category": "medicine | exercise | other",
  "times": ["08:00", "20:00"],
  "imageKey": "pill-icon-1",
  "photoStoragePath": "medicine-photos/{uid}/{reminderId}.jpg",
  "analysis": {
    "drugName": "타이레놀",
    "dosage": "500mg",
    "frequency": "1일 3회",
    "timings": ["08:00", "14:00", "20:00"],
    "rawText": "",
    "confidence": 0.85
  },
  "confirmed": true,
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### Subcollection: `users/{uid}/newsletters/{issueId}` (2단계)

```json
{
  "trigger": "alarm_dismiss | scheduled",
  "reminderId": "abc123",
  "title": "오늘의 복약 팁",
  "body": "...",
  "tips": ["...", "..."],
  "status": "pending | sent | failed",
  "scheduledAt": "Timestamp",
  "sentAt": "Timestamp"
}
```

### Subcollection: `users/{uid}/devices/{deviceToken}` (FCM)

```json
{
  "platform": "android | pc",
  "fcmToken": "...",
  "updatedAt": "Timestamp"
}
```

---

## 4. Storage 경로

| 경로 | 용도 |
|------|------|
| `medicine-photos/{uid}/{reminderId}.jpg` | 약 사진 원본 |
| `medicine-photos/{uid}/{reminderId}_thumb.jpg` | (선택) 썸네일 |

- max 5MB, `image/jpeg` / `image/png`
- 업로드: **서버 Admin SDK** (`StorageService.uploadMedicinePhoto`)

---

## 5. FCM 메시지 (2단계 참고)

```json
{
  "notification": {
    "title": "복약 알림",
    "body": "타이레놀 복용 시간입니다"
  },
  "data": {
    "type": "reminder_alarm",
    "reminderId": "abc123",
    "phase": "1"
  }
}
```

뉴스레터:

```json
{
  "data": {
    "type": "newsletter",
    "issueId": "xyz",
    "title": "...",
    "body": "..."
  }
}
```

---

## 6. Security Rules (draft — Java #2 deploy)

**Firestore** — 클라이언트 직접 접근 차단 (Admin only):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

**Storage** — 동일 (서버만):

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if false;
    }
  }
}
```

헤커톤 이후 Auth 연동 시 `request.auth.uid == uid` 규칙으로 완화 검토.

---

## 7. Spring Boot ↔ Firebase (패키지)

```
com.aac.firebase
├── FirebaseAppHolder          # 초기화 상태
├── FirestoreService           # interface — Java #1
├── FirebaseFirestoreService   # impl — Java #2
├── StorageService
├── FirebaseStorageService
├── FcmService
└── FirebaseFcmService
```

### Backend A (Firebase) — [HANDOFF-backend-firebase.md](./HANDOFF-backend-firebase.md)

- [ ] Firebase Console 프로젝트 생성
- [ ] `FirebaseFirestoreService` / `StorageService` / `FcmService` impl
- [ ] Security Rules deploy

### Backend B (Spring API) — [HANDOFF-backend-api.md](./HANDOFF-backend-api.md)

- [ ] `apps/server` scaffold + interface + Noop stub
- [ ] REST Controller (profile, reminders, health)
- [ ] `docs/openapi.yaml` (stub: [openapi-stub.md](./openapi-stub.md))

**설계자(Java #1) 연락 없이** PR 리뷰 + [TASKS.md](./TASKS.md) 로 진행.

---

## 8. REST API (1단계 — 프로필/리마인더)

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/v1/firebase/health` | Admin SDK 연결 상태 |
| PUT | `/api/v1/users/{uid}/profile` | 회원정보 저장 |
| GET | `/api/v1/users/{uid}/profile` | 회원정보 조회 |
| POST | `/api/v1/users/{uid}/reminders` | 알림 카테고리 추가 |
| GET | `/api/v1/users/{uid}/reminders` | 목록 |
| PATCH | `/api/v1/users/{uid}/reminders/{id}` | 수정·confirmed |
| POST | `/api/v1/users/{uid}/devices` | FCM 토큰 등록 |

---

## 9. 협업 Handoff

Java #2에게 전달:

1. 이 문서 + `docs/HANDOFF-java2.md`
2. `apps/server` clone 후 `application-local.yml` 설정
3. `FirebaseFirestoreService` 등 `@Service` 클래스 구현
4. PR 기준: `/api/v1/firebase/health` → `{ "firebase": "ok" }`

연락 포인트:

- 스키마 변경 → Java #1과 DTO sync
- credentials → `.gitignore` 준수, Slack/1Password 공유

---

## 10. 로컬 실행

```bash
cd apps/server
# Java 17+, Maven
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# firebase-service-account.json 경로 설정
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
curl http://localhost:8080/api/v1/firebase/health
```

Firebase credentials 없으면 `{ "firebase": "disabled" }` — API 골격은 기동 가능.
