# Handoff — Java #1 (계정·정보관리)

> **패키지:** `com.aac.account.*`  
> **선행:** 서버 실행 가능 ([TEAM.md](./TEAM.md))  
> **협업:** Java #2 — [architecture.md](./architecture.md) 이벤트 계약

## 담당 기능

- [ ] 회원가입 / 로그인 + **JWT** 발급·검증
- [ ] 부모-자녀 **페어링** (6자리 코드 생성·검증·매핑)
- [ ] 복용약 / 운동 / 기타 **알림 CRUD** (기존 `ReminderController` 확장)
- [ ] 알림 **카테고리·시간** 설정
- [ ] **정시 리마인더** 스케줄링 (`@Scheduled` → FCM 또는 Java #2 이벤트)
- [ ] **마이페이지** API

## 시작 체크리스트

1. [ ] 레포 clone + `.\run-server.ps1` → health 200
2. [ ] [firebase-design.md](./firebase-design.md) 읽기
3. [ ] 브랜치: `feature/java1/auth-jwt`

## 구현 순서 (권장)

### Week 1 — Auth

- [ ] `spring-boot-starter-security` + JWT (jjwt 또는 nimbus)
- [ ] `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
- [ ] `Authorization: Bearer` 필터
- [ ] env: `JWT_SECRET`, `JWT_EXPIRY=86400000`

```yaml
# application-local.yml
app:
  jwt:
    secret: ${JWT_SECRET}
    expiry-ms: 86400000
```

### Week 1 — Pairing

- [ ] `POST /api/v1/pairing/code` — 부모가 6자리 코드 생성 (TTL 10분)
- [ ] `POST /api/v1/pairing/verify` — 자녀(또는 탭)가 코드 입력
- [ ] Firestore `pairings/{code}` + `users/{parentUid}/linkedUsers/{childUid}`

### Week 2 — CRUD (기존 코드 이전)

- [ ] `UserProfileController`, `ReminderController` → `com.aac.account.controller`
- [ ] JWT에서 `uid` 추출 — path `{uid}`와 토큰 일치 검증
- [ ] `GET /api/v1/users/me` (마이페이지)

### Week 2 — Scheduler

- [ ] `@Scheduled(cron = "0 * * * * *")` 매 분 reminders times 매칭
- [ ] 매칭 시 `ReminderDueEvent` 발행 → Java #2가 FCM 처리 (또는 직접 `FcmService` 호출 — [architecture.md](./architecture.md) 합의)

## Firestore (Java #1 작성)

```
pairings/{code}
  parentUid, expiresAt, used: boolean

users/{uid}/linkedUsers/{otherUid}
  role: "parent" | "child", linkedAt
```

## 검증 curl

```bash
# register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"pass1234","name":"홍길동","userType":"self"}'

# login → token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"pass1234"}'
```

## Java #2와 맞출 것

- CRUD 변경 시 `ReminderChangedEvent` / `ProfileChangedEvent` 발행 (interface는 `com.aac.sync.event`에 Java #2가 정의해도 됨)
- 스케줄 알림: Java #2 `FcmService` 주입 vs 이벤트 — **첫 PR 전 15분 sync 미팅**

## 막히면

- [TASKS.md](./TASKS.md) 블로커 로그
- 스키마 변경 → `firebase-design.md` PR
