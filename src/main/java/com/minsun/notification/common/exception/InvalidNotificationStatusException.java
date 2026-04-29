package com.minsun.notification.common.exception;

import com.minsun.notification.domain.NotificationStatus;

public class InvalidNotificationStatusException extends RuntimeException {
    public InvalidNotificationStatusException(Long id, NotificationStatus status) {
        super("Invalid status transition for notification " + id + ": " + status);
    }
}
