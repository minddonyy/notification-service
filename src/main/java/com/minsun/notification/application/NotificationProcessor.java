package com.minsun.notification.application;

import com.minsun.notification.common.exception.UnsupportedChannelException;
import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.NotificationRepository;
import com.minsun.notification.infrastructure.sender.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessor {

    private final List<NotificationSender> senders;
    private final NotificationRepository repository;

    @Transactional
    public void process(Notification notification) {
        notification.markProcessing();
        repository.save(notification);

        try {
            NotificationSender sender = senders.stream()
                    .filter(s -> s.supports(notification.getChannel()))
                    .findFirst()
                    .orElseThrow(() -> new UnsupportedChannelException(notification.getChannel()));

            sender.send(notification);
            notification.markSent();

        } catch (Exception e) {
            log.error("Failed to send notification id={}, reason={}", notification.getId(), e.getMessage());
            notification.markFailed(e.getMessage());
        }

        repository.save(notification);
    }
}
