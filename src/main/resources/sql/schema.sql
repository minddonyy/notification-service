CREATE TABLE notifications
(
    id                BIGSERIAL PRIMARY KEY,
    idempotency_key   VARCHAR(255) NOT NULL,
    recipient_id      BIGINT       NOT NULL,
    notification_type VARCHAR(50)  NOT NULL,
    reference_type    VARCHAR(50)  NOT NULL,
    reference_id      BIGINT       NOT NULL,
    channel           VARCHAR(20)  NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count       INT          NOT NULL DEFAULT 0,
    max_retry_count   INT          NOT NULL DEFAULT 3,
    next_retry_at     TIMESTAMP,
    scheduled_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    processed_at      TIMESTAMP,
    failure_reason    TEXT,
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at           TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key)
);

-- PENDING 건 폴링용 (status, scheduled_at)
CREATE INDEX idx_notifications_status_scheduled
    ON notifications (status, scheduled_at)
    WHERE status = 'PENDING';

-- 수신자별 목록 조회용
CREATE INDEX idx_notifications_recipient
    ON notifications (recipient_id, created_at DESC);

-- Stuck PROCESSING 감지용
CREATE INDEX idx_notifications_stuck
    ON notifications (status, updated_at)
    WHERE status = 'PROCESSING';

-- ShedLock
CREATE TABLE shedlock
(
    name       VARCHAR(64)  NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);

-- 알림 템플릿 (선택 구현)
CREATE TABLE notification_templates
(
    id                BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(50)  NOT NULL,
    channel           VARCHAR(20)  NOT NULL,
    title_template    VARCHAR(255) NOT NULL,
    body_template     TEXT         NOT NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_template_type_channel UNIQUE (notification_type, channel)
);
