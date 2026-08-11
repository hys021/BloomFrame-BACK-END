# OpenAPI stub — AAC Server (Phase 1)

> Backend B가 scaffold와 함께 `docs/openapi.yaml` 파일로 export. Android 팀 연동 SSOT.

전체 YAML은 [openapi-stub-content](#yaml-본문) 참고. Backend B PR 시 `docs/openapi.yaml` 생성.

## Android 연동 (Phase 2)

```
EXPO_PUBLIC_API_URL=http://<PC_LAN_IP>:8080
EXPO_PUBLIC_USER_ID=demo-user
```

## YAML 본문

```yaml
openapi: 3.0.3
info:
  title: AAC Server API
  version: 0.1.0
servers:
  - url: http://localhost:8080
paths:
  /api/v1/firebase/health:
    get:
      responses:
        "200":
          content:
            application/json:
              schema:
                type: object
                properties:
                  firebase: { type: string, enum: [ok, disabled, error] }
                  projectId: { type: string }
  /api/v1/users/{uid}/profile:
    get: { summary: 회원정보 조회 }
    put: { summary: 회원정보 저장 }
  /api/v1/users/{uid}/reminders:
    get: { summary: 알림 목록 }
    post: { summary: 알림 추가 }
  /api/v1/users/{uid}/reminders/{id}:
    patch: { summary: 알림 수정 }
```

상세 스키마: [firebase-design.md §8](./firebase-design.md)
