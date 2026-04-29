package com.minsun.notification.api.dto;

import com.minsun.notification.domain.NotificationChannel;
import com.minsun.notification.domain.NotificationType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record NotificationCreateRequest(
        @NotNull Long recipientId,
        @NotNull NotificationType notificationType,
        @NotNull String referenceType,
        @NotNull Long referenceId,
        @NotNull NotificationChannel channel,
        LocalDateTime scheduledAt
) {}
