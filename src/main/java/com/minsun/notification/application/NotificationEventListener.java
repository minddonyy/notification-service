package com.minsun.notification.application;

import com.minsun.notification.infrastructure.event.NotificationCreatedEvent;
import com.minsun.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationProcessor processor;
    private final NotificationRepository repository;

    // AFTER_COMMIT: 등록 트랜잭션 커밋 이후 실행 → DB에 레코드가 존재함을 보장
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {
        log.info("NotificationCreatedEvent received: notificationId={}", event.notificationId());
        repository.findById(event.notificationId())
                .ifPresent(processor::process);
    }
}
