package com.minsun.notification.api.dto;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationChannel;
import com.minsun.notification.domain.NotificationStatus;
import com.minsun.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationSummaryResponse(
        Long notificationId,
        NotificationType notificationType,
        NotificationChannel channel,
        NotificationStatus status,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationSummaryResponse from(Notification n) {
        return new NotificationSummaryResponse(
                n.getId(),
                n.getNotificationType(),
                n.getChannel(),
                n.getStatus(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
