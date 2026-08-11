# BloomFrame Backend

Spring Boot API — Firebase Firestore/FCM + Cloudflare R2 인프라

- **프로젝트 ID:** `bloomframe-cbed6`
- **브랜치:** `develop`에서 개발, push는 팀 리드

## 실행

```powershell
git clone https://github.com/hys021/BloomFrame-BACK-END.git
cd BloomFrame-BACK-END\server
.\gradlew.bat bootRun
```

## 설정

1. `application-local.yml.example` → `application-local.yml` 복사 후 R2 키 입력 (**local.yml은 git 금지**)
2. `config/firebase-service-account.json` — Discord에서 수령 (**git 금지**)

```powershell
curl http://localhost:8080/api/v1/firebase/health
```

## API (인프라)

| Method | Path |
|--------|------|
| GET | `/api/v1/firebase/health` |

비즈니스 API (profile, reminders, auth 등)는 Java #1 / #2가 추가.
