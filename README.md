# BloomFrame Backend

멋쟁이사자처럼 14기 해커톤 — BloomFrame Spring Boot API

- **DB:** Firebase Firestore + FCM (Spark)
- **파일:** Cloudflare R2 (약 사진)
- **프로젝트 ID:** `bloomframe-cbed6`

## Quick start

```powershell
git clone https://github.com/hys021/BloomFrame-BACK-END.git
cd BloomFrame-BACK-END\server
.\gradlew.bat bootRun
```

설정: `src/main/resources/application-local.yml` (Firebase JSON + R2 키 입력)

```powershell
curl http://localhost:8080/api/v1/firebase/health
# Firebase/R2 미설정: {"firebase":"disabled","storage":"noop"}
# 설정 완료: {"firebase":"ok","storage":"r2","projectId":"bloomframe-cbed6"}
```

## Secrets

| Secret | 위치 |
|--------|------|
| `firebase-service-account.json` | `server/config/` (git 금지) |
| R2 keys | `application-local.yml` |
| `JWT_SECRET` | Java #1 (auth 구현 시) |

## API

| Method | Path |
|--------|------|
| GET | `/api/v1/firebase/health` |
| PUT/GET | `/api/v1/users/{uid}/profile` |
| POST/GET | `/api/v1/users/{uid}/reminders` |
| PATCH | `/api/v1/users/{uid}/reminders/{id}` |
| POST | `/api/v1/users/{uid}/devices` |

## 역할 분담

| | Java #1 | Java #2 |
|---|---------|---------|
| **패키지** | `com.bloomframe.server.account.*` | `com.bloomframe.server.sync.*` |
| **담당** | JWT, 페어링, CRUD, 스케줄 | auth-touch, 꽃 상태, FCM, sync |

공통 Firebase: `com.bloomframe.server.firebase.*`

프론트(Android/PC)는 별도 레포 — REST API만 연동.

## Firestore Rules

Firebase Console → Firestore → Rules에서 **클라이언트 직접 접근 차단** (서버 Admin SDK만 사용):

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
