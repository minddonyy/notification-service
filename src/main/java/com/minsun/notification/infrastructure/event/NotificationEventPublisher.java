package com.minsun.notification.infrastructure.event;

public interface NotificationEventPublisher {
    void publish(NotificationCreatedEvent event);
}
