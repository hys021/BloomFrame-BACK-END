# Handoff — Java #2 (실시간 동기화·인증)

> **패키지:** `com.aac.sync.*`  
> **선행:** Firebase JSON ([TEAM.md](./TEAM.md) Secrets)  
> **협업:** Java #1 — [architecture.md](./architecture.md)

## 담당 기능

- [ ] 앱 ↔ **탭(PC)** 양방향 실시간 반영 (정보 수정, 이미지, 인증 기록)
- [ ] **인증 로그** 영구 저장 + 조회 API
- [ ] **터치 인증** API 수신 + Firebase write
- [ ] **미인증 감지** → 시듦 상태 트리거 (IoT 플로우 a/b)
- [ ] **뉴스레터** 표시 트리거 (인증 완료 → FCM push)

## 시작 체크리스트

1. [ ] `firebase-service-account.json` 수령 → `apps/server/config/`
2. [ ] `application-local.yml` 설정
3. [ ] `.\run-server.ps1` → `{"firebase":"ok"}`
4. [ ] 브랜치: `feature/java2/auth-touch`

## Firebase Console (Java #2 또는 팀 리드)

- [ ] 프로젝트 생성
- [ ] Firestore / Storage / FCM
- [ ] Rules deploy ([firebase/firestore.rules](../firebase/firestore.rules))
- [ ] JSON 팀 공유

## 구현 순서 (권장)

### Phase A — Firebase 연결

- [ ] health → `ok` 확인
- [ ] `FirebaseFirestoreService` 실 Firestore read/write 테스트
- [ ] `firebase deploy --only firestore:rules,storage`

### Phase B — 터치 인증

- [ ] `POST /api/v1/auth-touch`  
  Body: `{ reminderId, touchedAt }` + JWT  
- [ ] Firestore `users/{uid}/authLogs/{logId}` 저장
- [ ] `AuthCompletedEvent` 발행

```json
{
  "reminderId": "abc",
  "touchedAt": "2026-08-07T14:30:00Z",
  "source": "android",
  "phase": 1
}
```

### Phase C — Flower state (IoT a/b)

- [ ] `users/{uid}/flowerState` — `healthy | withering | withered`
- [ ] 알림 발송 후 **3분** 미인증 → `withering` + 2차 알림 (FCM)
- [ ] **10분** 내 해제 → `healthy` (플로우 a) / `reset` (플로우 b)
- [ ] `@Scheduled` 또는 Java #1 스케줄 이벤트 구독

참고: PPT IoT 플로우 — [architecture.md](./architecture.md)

### Phase D — 실시간 동기화

**옵션 A (헤커톤 권장):** Firestore snapshot + FCM data message  
**옵션 B:** 기존 [apps/relay](../apps/relay) WebSocket + `profile.sync` 확장

- [ ] Java #1 `ReminderChangedEvent` 구독 → FCM `type=reminder_updated` → 탭 갱신
- [ ] 이미지 변경: Storage URL → sync payload

### Phase E — 뉴스레터 트리거

- [ ] `AuthCompletedEvent` 수신 → `users/{uid}/newsletters` pending doc
- [ ] FCM data: `{ type: "newsletter", issueId, title, body }`
- [ ] (본문 생성은 2단계 OpenAI — mock 문자열로 MVP)

## Firestore (Java #2 작성)

```
users/{uid}/authLogs/{logId}
  reminderId, touchedAt, source, onTime: boolean

users/{uid}/flowerState
  state, lastReminderId, phase, updatedAt, nextAlarmAt

users/{uid}/newsletters/{issueId}
  title, body, status, sentAt
```

## API

| Method | Path | 설명 |
|--------|------|------|
| POST | `/api/v1/auth-touch` | 터치 인증 |
| GET | `/api/v1/users/{uid}/auth-logs` | 로그 조회 |
| GET | `/api/v1/users/{uid}/flower` | 꽃 상태 |
| POST | `/api/v1/sync/notify` | (internal) 변경 알림 |

## 검증

```bash
curl -X POST http://localhost:8080/api/v1/auth-touch \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reminderId":"1","touchedAt":"2026-08-07T14:30:00Z"}'

curl http://localhost:8080/api/v1/users/demo/flower
```

## Java #1과 맞출 것

- `com.aac.sync.event.*` 패키지에 이벤트 클래스 정의 → Java #1이 subscribe 또는 publish
- FCM token: `POST /users/{uid}/devices` (기존) — Java #1 등록, Java #2 발송

## 막히면

- [TASKS.md](./TASKS.md) 블로커
- Relay vs FCM 선택 → TASKS에 결정 기록
