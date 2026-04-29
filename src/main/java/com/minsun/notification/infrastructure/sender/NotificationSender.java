package com.minsun.notification.infrastructure.sender;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationChannel;

public interface NotificationSender {
    boolean supports(NotificationChannel channel);
    void send(Notification notification);
}
