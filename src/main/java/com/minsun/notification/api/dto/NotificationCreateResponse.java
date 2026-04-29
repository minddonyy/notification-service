package com.minsun.notification.api.dto;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationStatus;

public record NotificationCreateResponse(
        Long notificationId,
        String idempotencyKey,
        NotificationStatus status,
        boolean isNew
) {
    public static NotificationCreateResponse ofNew(Notification n) {
        return new NotificationCreateResponse(n.getId(), n.getIdempotencyKey(), n.getStatus(), true);
    }

    public static NotificationCreateResponse ofDuplicate(Notification n) {
        return new NotificationCreateResponse(n.getId(), n.getIdempotencyKey(), n.getStatus(), false);
    }
}
