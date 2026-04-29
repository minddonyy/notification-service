package com.minsun.notification.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationEventNotificationPublisher implements NotificationEventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(NotificationCreatedEvent event) {
        publisher.publishEvent(event);
    }
}
