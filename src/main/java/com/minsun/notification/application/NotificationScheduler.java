package com.minsun.notification.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationPollingHelper pollingHelper;
    private final NotificationProcessor processor;

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
}
