# BloomFrame Backend

멋쟁이사자처럼 14기 해커톤 — BloomFrame Spring Boot API

- **DB:** Firebase Firestore + FCM (Spark)
- **파일:** Cloudflare R2 (약 사진)
- **레포:** https://github.com/hys021/BloomFrame-BACK-END

## Quick start (Firebase 없이 — noop)

```powershell
cd server
.\gradlew.bat bootRun
```

```powershell
curl http://localhost:8080/api/v1/firebase/health
# {"firebase":"disabled","storage":"noop"}
```

## Firebase + R2 연결

1. `server/src/main/resources/application-local.yml.example` → `application-local.yml` 복사
2. Discord에서 받은 `firebase-service-account.json` → `server/config/`
3. `application-local.yml`에 R2 키 입력
4. 실행:

```powershell
cd server
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

상세: [docs/openapi-stub.md](docs/openapi-stub.md)

## 팀 문서

| 문서 | 내용 |
|------|------|
| [docs/TEAM.md](docs/TEAM.md) | 온보딩 |
| [docs/architecture.md](docs/architecture.md) | Java #1 / #2 경계 |
| [docs/firebase-design.md](docs/firebase-design.md) | Firestore 스키마 |
| [docs/HANDOFF-java1.md](docs/HANDOFF-java1.md) | JWT, CRUD, 페어링 |
| [docs/HANDOFF-java2.md](docs/HANDOFF-java2.md) | 인증, FCM, sync |

## Firestore Rules deploy

```bash
cd firebase
firebase deploy --only firestore:rules
```
