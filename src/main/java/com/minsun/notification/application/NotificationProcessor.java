package com.minsun.notification.application;

import com.minsun.notification.common.exception.UnsupportedChannelException;
import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessor {

    private final List<NotificationSender> senders;
    private final NotificationStatusUpdater statusUpdater;

    public void process(Notification notification) {
        NotificationSender sender;
        try {
            sender = senders.stream()
                    .filter(s -> s.supports(notification.getChannel()))
                    .findFirst()
                    .orElseThrow(() -> new UnsupportedChannelException(notification.getChannel()));
        } catch (UnsupportedChannelException e) {
            log.error("Unsupported channel for notification id={}, channel={}", notification.getId(), notification.getChannel());
            statusUpdater.markDeadLetter(notification, e.getMessage());
            return;
        }

        try {
            sender.send(notification);
            statusUpdater.markSent(notification);
        } catch (Exception e) {
            log.error("Failed to send notification id={}, reason={}", notification.getId(), e.getMessage());
            statusUpdater.markFailed(notification, e.getMessage());
        }
    }
}
