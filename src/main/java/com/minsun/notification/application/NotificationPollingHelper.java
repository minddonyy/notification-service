package com.minsun.notification.application;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
class NotificationPollingHelper {

    private final NotificationRepository repository;

    // FOR UPDATE SKIP LOCKED + markProcessing을 한 트랜잭션에서 처리
    // → 커밋 시점에 PROCESSING 상태가 DB에 반영되어 다른 인스턴스의 중복 처리 방지
    @Transactional
    public List<Notification> claimPending() {
        List<Notification> list = repository.findPendingForProcessing();
        list.forEach(Notification::markProcessing);
        repository.saveAll(list);
        return list;
    }
}
