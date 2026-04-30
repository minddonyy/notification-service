package com.minsun.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private Notification create() {
        return Notification.create("key", 1L, NotificationType.ENROLLMENT_COMPLETE,
                "COURSE", 1L, NotificationChannel.EMAIL, null);
    }

    @Test
    @DisplayName("생성 시 초기 상태는 PENDING, retryCount=0, isRead=false")
    void create_초기상태() {
        Notification n = create();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getRetryCount()).isZero();
        assertThat(n.isRead()).isFalse();
        assertThat(n.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markProcessing 호출 시 PROCESSING 상태로 전이")
    void markProcessing_상태전이() {
        Notification n = create();
        n.markProcessing();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PROCESSING);
    }

    @Test
    @DisplayName("markSent 호출 시 SENT 상태로 전이 및 processedAt 기록")
    void markSent_상태전이() {
        Notification n = create();
        n.markSent();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(n.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("markFailed 호출 시 FAILED 상태, retryCount 증가, nextRetryAt 설정")
    void markFailed_재시도가능한경우() {
        Notification n = create();
        n.markFailed("connection timeout");
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(n.getRetryCount()).isEqualTo(1);
        assertThat(n.getNextRetryAt()).isNotNull();
        assertThat(n.getFailureReason()).isEqualTo("connection timeout");
    }

    @Test
    @DisplayName("markFailed를 maxRetryCount 이상 호출 시 DEAD_LETTER 전이")
    void markFailed_최대재시도초과_DEAD_LETTER() {
        Notification n = create(); // maxRetryCount = 3
        n.markFailed("fail1");
        n.markFailed("fail2");
        n.markFailed("fail3");
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.DEAD_LETTER);
        assertThat(n.getRetryCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("scheduleRetry 호출 시 PENDING 복귀 및 nextRetryAt 초기화")
    void scheduleRetry_PENDING복귀() {
        Notification n = create();
        n.markFailed("timeout");
        n.scheduleRetry();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getNextRetryAt()).isNull();
    }

    @Test
    @DisplayName("resetForAdminRetry 호출 시 PENDING 복귀 및 retryCount 초기화")
    void resetForAdminRetry_retryCount초기화() {
        Notification n = create();
        n.markFailed("fail1");
        n.markFailed("fail2");
        n.resetForAdminRetry();
        assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(n.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("markRead 호출 시 isRead=true, readAt 기록")
    void markRead_읽음처리() {
        Notification n = create();
        n.markRead();
        assertThat(n.isRead()).isTrue();
        assertThat(n.getReadAt()).isNotNull();
    }
}
