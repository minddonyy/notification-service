package com.minsun.notification.infrastructure.sender;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        // 실제 운영 시 JavaMailSender로 교체
        log.info("[EMAIL MOCK] recipientId={}, type={}, referenceId={}",
                notification.getRecipientId(),
                notification.getNotificationType(),
                notification.getReferenceId());
    }
}
