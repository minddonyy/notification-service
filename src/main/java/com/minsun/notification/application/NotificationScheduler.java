package com.minsun.notification.application;

import com.minsun.notification.infrastructure.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationPollingHelper pollingHelper;
    private final NotificationProcessor processor;
    private final NotificationRepository repository;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "notification-polling", lockAtMostFor = "PT10S", lockAtLeastFor = "PT5S")
    public void pollPending() {
        var pending = pollingHelper.claimPending();
        if (!pending.isEmpty()) {
            log.info("Polling {} pending notification(s)", pending.size());
            pending.forEach(processor::process);
        }
    }

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "notification-retry", lockAtMostFor = "PT10S", lockAtLeastFor = "PT5S")
    public void retryFailed() {
        pollingHelper.requeueFailed();
    }

    // PROCESSING 상태가 10분 이상 지속된 stuck 건 → PENDING 복구
    @Transactional
    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "notification-stuck-recovery", lockAtMostFor = "PT55S", lockAtLeastFor = "PT30S")
    public void recoverStuck() {
        int recovered = repository.recoverStuckProcessing();
        if (recovered > 0) {
            log.info("Recovered {} stuck PROCESSING notification(s) to PENDING", recovered);
        }
    }
}
