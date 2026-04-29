package com.minsun.notification.api.dto;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationChannel;
import com.minsun.notification.domain.NotificationStatus;
import com.minsun.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationDetailResponse(
        Long notificationId,
        Long recipientId,
        NotificationType notificationType,
        String referenceType,
        Long referenceId,
        NotificationChannel channel,
        NotificationStatus status,
        int retryCount,
        LocalDateTime scheduledAt,
        LocalDateTime processedAt,
        String failureReason,
        LocalDateTime createdAt
) {
    public static NotificationDetailResponse from(Notification n) {
        return new NotificationDetailResponse(
                n.getId(),
                n.getRecipientId(),
                n.getNotificationType(),
                n.getReferenceType(),
                n.getReferenceId(),
                n.getChannel(),
                n.getStatus(),
                n.getRetryCount(),
                n.getScheduledAt(),
                n.getProcessedAt(),
                n.getFailureReason(),
                n.getCreatedAt()
        );
    }
}
