package com.minsun.notification.infrastructure.sender;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InAppNotificationSender implements NotificationSender {

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.IN_APP;
    }

    @Override
    public void send(Notification notification) {
        log.info("[IN_APP] recipientId={}, type={}, referenceId={}",
                notification.getRecipientId(),
                notification.getNotificationType(),
                notification.getReferenceId());
    }
}
