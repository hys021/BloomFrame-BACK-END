# BloomFrame Backend

멋쟁이사자처럼 14기 해커톤 — BloomFrame Spring Boot API

- **DB:** Firebase Firestore + FCM (Spark)
- **파일:** Cloudflare R2 (약 사진)
- **프로젝트 ID:** `bloomframe-cbed6`

## Quick start (Firebase 없이 — noop)

```powershell
git clone https://github.com/hys021/BloomFrame-BACK-END.git
cd BloomFrame-BACK-END\server
.\gradlew.bat bootRun
```

```powershell
curl http://localhost:8080/api/v1/firebase/health
# {"firebase":"disabled","storage":"noop"}
```

## Firebase + R2 연결

1. `src/main/resources/application-local.yml.example` → `application-local.yml` 복사
2. Discord에서 받은 `firebase-service-account.json` → `config/`
3. `application-local.yml`에 R2 키 입력
4. 실행:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

```powershell
curl http://localhost:8080/api/v1/firebase/health
# {"firebase":"ok","storage":"r2","projectId":"bloomframe-cbed6"}
```

## Secrets (git 금지 — Discord)

| Secret | 위치 |
|--------|------|
| `firebase-service-account.json` | `server/config/` |
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
