package com.minsun.notification.application;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class NotificationStatusUpdater {

    private final NotificationRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(Notification notification) {
        notification.markProcessing();
        repository.save(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(Notification notification) {
        notification.markSent();
        repository.save(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Notification notification, String reason) {
        notification.markFailed(reason);
        repository.save(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDeadLetter(Notification notification, String reason) {
        notification.markDeadLetterDirectly(reason);
        repository.save(notification);
    }
}
