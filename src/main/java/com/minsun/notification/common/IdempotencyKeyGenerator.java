package com.minsun.notification.common;

import com.minsun.notification.domain.NotificationChannel;
import com.minsun.notification.domain.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyKeyGenerator {

    // {recipientId}:{notificationType}:{referenceType}:{referenceId}:{channel}
    public String generate(Long recipientId, NotificationType type, String referenceType, Long referenceId, NotificationChannel channel) {
        return recipientId + ":" + type + ":" + referenceType + ":" + referenceId + ":" + channel;
    }
}
