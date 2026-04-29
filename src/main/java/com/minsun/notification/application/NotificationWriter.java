package com.minsun.notification.application;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class NotificationWriter {

    private final NotificationRepository repository;

    // REQUIRES_NEW: INSERT 실패 시 이 트랜잭션만 롤백되어 외부 트랜잭션이 오염되지 않음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification save(Notification notification) {
        return repository.saveAndFlush(notification);
    }
}
