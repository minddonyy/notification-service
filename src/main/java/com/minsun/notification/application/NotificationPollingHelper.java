package com.minsun.notification.application;

import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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

    // next_retry_at이 지난 FAILED 건을 PENDING으로 복귀 → 폴링 스케줄러가 재처리
    @Transactional
    public void requeueFailed() {
        List<Notification> failed = repository.findFailedForRetry();
        if (failed.isEmpty()) return;

        failed.forEach(Notification::scheduleRetry);
        repository.saveAll(failed);
        log.info("Re-queued {} failed notification(s) to PENDING", failed.size());
    }
}
