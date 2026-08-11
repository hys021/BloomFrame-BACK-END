# BloomFrame — 백엔드 온보딩

> clone → `server/` → secrets → `gradlew bootRun`

## Day 1

```powershell
git clone https://github.com/hys021/BloomFrame-BACK-END.git
cd BloomFrame-BACK-END/server
```

### Secrets (Discord — git 금지)

| 파일 | 저장 위치 |
|------|-----------|
| `firebase-service-account.json` | `server/config/` |
| `application-local.yml` | `server/src/main/resources/` |

```powershell
copy src\main\resources\application-local.yml.example src\main\resources\application-local.yml
# yaml 수정 후
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
curl http://localhost:8080/api/v1/firebase/health
```

## 역할

| | Java #1 | Java #2 |
|---|---------|---------|
| **Handoff** | [HANDOFF-java1.md](./HANDOFF-java1.md) | [HANDOFF-java2.md](./HANDOFF-java2.md) |
| **패키지** | `com.bloomframe.server.account.*` | `com.bloomframe.server.sync.*` |
| **담당** | JWT, 페어링, CRUD, 스케줄 | auth-touch, 꽃 상태, FCM, sync |

공통 Firebase: `com.bloomframe.server.firebase.*`

## 스택

| 항목 | 기술 |
|------|------|
| DB | Firebase Firestore |
| 파일 | Cloudflare R2 |
| Push | Firebase FCM |
| JDK | 17+ |

프론트(Android/PC)는 **별도 레포** — REST API만 연동.

## SSOT

- [architecture.md](./architecture.md)
- [firebase-design.md](./firebase-design.md)
- [openapi-stub.md](./openapi-stub.md)
