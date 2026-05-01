# 알림 발송 시스템

## 프로젝트 개요

수강 신청 완료, 결제 확인 등 다양한 이벤트에 대해 EMAIL / IN_APP 채널로 알림을 발송하는 백엔드 시스템입니다.

---

## 기술 스택

| 항목 | 선택 | 비고 |
|------|------|------|
| Language | Java 21 | Virtual Thread 활성화 |
| Framework | Spring Boot 3.5 | |
| ORM | Spring Data JPA + Hibernate | |
| DB | PostgreSQL 16 | `FOR UPDATE SKIP LOCKED` 지원 |
| 분산 락 | ShedLock 6.9 | 다중 인스턴스 스케줄러 중복 실행 방지 |
| 비동기 | `ApplicationEventPublisher` + `@Async` | 브로커 없이 이벤트 드리븐 구조 |
| 문서 | SpringDoc OpenAPI (Swagger) 2.8 | |
| 빌드 | Gradle 8 | |

---

## 실행 방법

### 사전 요구사항
- Java 21
- Docker

### 1. DB 실행

```bash
docker compose up -d
```

### 2. 스키마 적용

```bash
docker exec -i notification-postgres psql -U notification -d notification \
  < src/main/resources/sql/schema.sql
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. API 확인

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## 요구사항 해석 및 가정

**"알림 처리 실패가 비즈니스 트랜잭션에 영향을 주어서는 안 된다. 단, 예외를 단순히 무시해서는 안 된다"**

→ 알림 발송 실패를 `try-catch`로 삼켜버리는 방식이 아니라 **상태 머신으로 관리**하는 것으로 해석했습니다.
실패는 `FAILED` 상태와 `failure_reason`으로 기록하고, 재시도 정책에 따라 반드시 재처리됩니다.
비즈니스 트랜잭션(수강 신청, 결제)과 알림 트랜잭션을 분리하여 알림 실패가 비즈니스 롤백을 유발하지 않도록 설계했습니다.

**"실제 메시지 브로커 없이 구현하되, 전환 가능한 구조"**

→ `NotificationEventPublisher` 인터페이스를 두고 현재는 `ApplicationEventPublisher` 구현체를 사용합니다.
Kafka/SQS 도입 시 구현체 교체만으로 전환 가능하며, DB Polling 레이어는 Dead Letter 복구용으로 유지합니다.

**"동일 이벤트에 대해 알림이 중복 발송되면 안 된다"**

→ `idempotency_key`를 별도 컬럼으로 두고, 중복 요청 시 기존 건을 반환하는 방식으로 API 멱등성을 보장합니다.
단순 Unique 제약만으로는 동시 요청 시나리오를 완전히 커버하지 못하므로, `REQUIRES_NEW` 트랜잭션에서 `saveAndFlush`를 통해 DB 제약 충돌을 즉시 감지하고 처리합니다.

---

## 설계 결정과 이유

### 1. Rich Domain Model

상태 전이(`markFailed`, `markSent`, `scheduleRetry` 등)를 `Notification` 엔티티 내부에 캡슐화했습니다.
Service가 엔티티 필드를 직접 조작하는 Anemic Domain Model을 방지하고, 도메인 규칙이 한 곳에서 관리됩니다.

### 2. Strategy Pattern으로 채널별 발송 분리

`NotificationSender` 인터페이스와 채널별 구현체(`EmailNotificationSender`, `InAppNotificationSender`)로 분리했습니다.
`NotificationProcessor`는 구현체를 몰라도 되며, 새 채널 추가 시 기존 코드를 수정하지 않습니다.

### 3. 이중 중복 처리 방지

| 방어선 | 역할 |
|--------|------|
| ShedLock | 동일 시각에 한 인스턴스만 Scheduler 실행 |
| `FOR UPDATE SKIP LOCKED` | 동일 row를 여러 인스턴스가 동시에 처리하는 것 방지 |

### 4. 비동기 이중 레이어

```
Layer 1 — ApplicationEvent (@Async)
  API 요청 → DB 저장 → 즉시 이벤트 발행 → 비동기 발송 시도

Layer 2 — @Scheduled Polling (안전망)
  매 5초마다 PENDING 건 폴링 → 미처리 건, 재시도 건 처리
  매 60초마다 PROCESSING stuck 건 복구
```

Layer 1이 실패하거나 서버가 재시작되어도 Layer 2가 자동으로 복구합니다.

### 5. Exponential Backoff 재시도

| 실패 횟수 | 다음 재시도 |
|-----------|------------|
| 1회 | 1분 뒤 |
| 2회 | 4분 뒤 |
| 3회 | 9분 뒤 |
| 3회 초과 | DEAD_LETTER |

공식: `delay(분) = retryCount²`

---

## 미구현 / 제약사항

| 항목 | 상태 | 비고 |
|------|------|------|
| Dead Letter 수동 재시도 API | 미구현 | `POST /api/v1/admin/notifications/{id}/retry` |
| 읽음 처리 API | 미구현 | `PATCH /api/v1/notifications/{id}/read` |
| 알림 템플릿 관리 | 미구현 | `NotificationTemplate` 엔티티/API |
| EMAIL 실제 발송 | Mock | 실제 운영 시 `JavaMailSender`로 교체 |
| IN_APP Push | 로그 처리 | DB 상태 관리 기반 조회 방식 |

---

## AI 활용 범위

- 기술 스택 선택 근거 검토 (AI 제안 → 본인 판단으로 확정)
- 시스템 설계 초안 작성 (AI 초안 → 도메인 모델, 상태 전이 직접 검토 및 수정)
- spec.md 문서 초안 작성 (AI 생성 → 항목별 직접 검증)
- 코드 구현: AI 보조 후 로직 직접 검토 및 수정

---

## API 목록 및 예시

### 공통

- Base URL: `/api/v1`
- 인증: `X-User-Id: {userId}` 헤더

---

### 알림 발송 요청 등록

**POST** `/api/v1/notifications`

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "recipientId": 42,
    "notificationType": "ENROLLMENT_COMPLETE",
    "referenceType": "COURSE",
    "referenceId": 100,
    "channel": "EMAIL",
    "scheduledAt": null
  }'
```

**신규 등록 — 201 Created**
```json
{
  "notificationId": 1,
  "idempotencyKey": "42:ENROLLMENT_COMPLETE:COURSE:100:EMAIL",
  "status": "PENDING",
  "isNew": true
}
```

**중복 요청 — 200 OK**
```json
{
  "notificationId": 1,
  "idempotencyKey": "42:ENROLLMENT_COMPLETE:COURSE:100:EMAIL",
  "status": "PENDING",
  "isNew": false
}
```

---

### 알림 상태 조회

**GET** `/api/v1/notifications/{notificationId}`

```bash
curl http://localhost:8080/api/v1/notifications/1 \
  -H "X-User-Id: 1"
```

```json
{
  "notificationId": 1,
  "recipientId": 42,
  "notificationType": "ENROLLMENT_COMPLETE",
  "referenceType": "COURSE",
  "referenceId": 100,
  "channel": "EMAIL",
  "status": "SENT",
  "retryCount": 0,
  "scheduledAt": "2024-01-01T09:00:00",
  "processedAt": "2024-01-01T09:00:05",
  "failureReason": null,
  "createdAt": "2024-01-01T08:59:00"
}
```

---

### 사용자 알림 목록 조회

**GET** `/api/v1/notifications?recipientId={id}&isRead={bool}&page={n}&size={n}`

```bash
curl "http://localhost:8080/api/v1/notifications?recipientId=42&isRead=false&page=0&size=20" \
  -H "X-User-Id: 1"
```

```json
{
  "content": [
    {
      "notificationId": 1,
      "notificationType": "ENROLLMENT_COMPLETE",
      "channel": "IN_APP",
      "status": "SENT",
      "isRead": false,
      "createdAt": "2024-01-01T09:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

---

## 데이터 모델 설명

### notifications

| 컬럼 | 설명 |
|------|------|
| `idempotency_key` | `{recipientId}:{notificationType}:{referenceType}:{referenceId}:{channel}` UNIQUE |
| `status` | `PENDING` → `PROCESSING` → `SENT` / `FAILED` → `DEAD_LETTER` |
| `retry_count` | 현재 재시도 횟수 |
| `max_retry_count` | 최대 재시도 횟수 (기본값 3) |
| `next_retry_at` | 다음 재시도 예정 시각 (Exponential Backoff) |
| `scheduled_at` | 발송 예약 시각 (미래 시각 설정 시 예약 발송) |
| `failure_reason` | 실패 사유 기록 |
| `is_read` | 읽음 여부 (IN_APP 전용) |

### 상태 전이

```
PENDING
  └─► PROCESSING
          ├─► SENT
          └─► FAILED
                ├─► PENDING   (retry_count < max_retry_count)
                └─► DEAD_LETTER (retry_count >= max_retry_count)
```

---

## 테스트 실행 방법

> DB가 실행 중이어야 합니다. (`docker compose up -d`)

```bash
./gradlew test
```

### 테스트 구성

| 클래스 | 종류 | 개수 | 검증 내용 |
|--------|------|------|-----------|
| `NotificationTest` | 단위 | 8개 | 엔티티 상태 전이, DEAD_LETTER, scheduleRetry |
| `RetryPolicyCalculatorTest` | 단위 | 3개 | Exponential Backoff 계산 (1²=1분, 2²=4분, 3²=9분) |
| `NotificationServiceTest` | 단위 | 3개 | 멱등성 — 신규 isNew=true, 중복 isNew=false, 404 예외 |
| `NotificationControllerIntegrationTest` | 통합 | 6개 | API 201/200/200/404/200/400 응답 검증 |
