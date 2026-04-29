package com.minsun.notification.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record NotificationListResponse(
        List<NotificationSummaryResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
    public static NotificationListResponse from(Page<NotificationSummaryResponse> page) {
        return new NotificationListResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}
