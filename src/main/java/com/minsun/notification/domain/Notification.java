package com.minsun.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String referenceType;

    @Column(nullable = false)
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private int maxRetryCount;

    private LocalDateTime nextRetryAt;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false)
    private boolean isRead;

    private LocalDateTime readAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Notification create(
            String idempotencyKey,
            Long recipientId,
            NotificationType notificationType,
            String referenceType,
            Long referenceId,
            NotificationChannel channel,
            LocalDateTime scheduledAt
    ) {
        Notification n = new Notification();
        n.idempotencyKey = idempotencyKey;
        n.recipientId = recipientId;
        n.notificationType = notificationType;
        n.referenceType = referenceType;
        n.referenceId = referenceId;
        n.channel = channel;
        n.status = NotificationStatus.PENDING;
        n.retryCount = 0;
        n.maxRetryCount = 3;
        n.scheduledAt = scheduledAt != null ? scheduledAt : LocalDateTime.now();
        n.isRead = false;
        n.createdAt = LocalDateTime.now();
        n.updatedAt = LocalDateTime.now();
        return n;
    }

    public void markProcessing() {
        this.status = NotificationStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.retryCount++;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();

        if (this.retryCount >= this.maxRetryCount) {
            this.status = NotificationStatus.DEAD_LETTER;
        } else {
            this.status = NotificationStatus.FAILED;
            // Exponential Backoff: delay(분) = retryCount²
            this.nextRetryAt = LocalDateTime.now().plusMinutes((long) retryCount * retryCount);
        }
    }

    public void scheduleRetry() {
        this.status = NotificationStatus.PENDING;
        this.nextRetryAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void resetForAdminRetry() {
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void recoverStuck() {
        this.status = NotificationStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }
}
