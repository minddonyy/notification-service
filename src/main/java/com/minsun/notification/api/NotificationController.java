package com.minsun.notification.api;

import com.minsun.notification.api.dto.*;
import com.minsun.notification.application.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationCreateResponse> register(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        NotificationCreateResponse response = notificationService.register(request);
        HttpStatus status = response.isNew() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{notificationId}")
    public NotificationDetailResponse findById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long notificationId
    ) {
        return notificationService.findById(notificationId);
    }

    @GetMapping
    public NotificationListResponse findByRecipientId(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long recipientId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return notificationService.findByRecipientId(recipientId, isRead, page, size);
    }
}
