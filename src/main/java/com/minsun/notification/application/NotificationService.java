package com.minsun.notification.application;

import com.minsun.notification.api.dto.*;
import com.minsun.notification.common.IdempotencyKeyGenerator;
import com.minsun.notification.common.exception.NotificationNotFoundException;
import com.minsun.notification.domain.Notification;
import com.minsun.notification.infrastructure.NotificationRepository;
import com.minsun.notification.infrastructure.event.NotificationCreatedEvent;
import com.minsun.notification.infrastructure.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationWriter writer;
    private final IdempotencyKeyGenerator keyGenerator;
    private final NotificationEventPublisher eventPublisher;

    @Transactional
    public NotificationCreateResponse register(NotificationCreateRequest request) {
        String key = keyGenerator.generate(
                request.recipientId(),
                request.notificationType(),
                request.referenceType(),
                request.referenceId(),
                request.channel()
        );

        Notification notification = Notification.create(
                key,
                request.recipientId(),
                request.notificationType(),
                request.referenceType(),
                request.referenceId(),
                request.channel(),
                request.scheduledAt()
        );

        try {
            Notification saved = writer.save(notification);
            eventPublisher.publish(new NotificationCreatedEvent(saved.getId()));
            return NotificationCreateResponse.ofNew(saved);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청 등으로 unique 충돌 → 기존 건 반환
            Notification existing = repository.findByIdempotencyKey(key)
                    .orElseThrow(() -> new NotificationNotFoundException(-1L));
            return NotificationCreateResponse.ofDuplicate(existing);
        }
    }

    @Transactional(readOnly = true)
    public NotificationDetailResponse findById(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        return NotificationDetailResponse.from(notification);
    }

    @Transactional(readOnly = true)
    public NotificationListResponse findByRecipientId(Long recipientId, Boolean isRead, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<NotificationSummaryResponse> result = (isRead != null)
                ? repository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, isRead, pageable)
                        .map(NotificationSummaryResponse::from)
                : repository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable)
                        .map(NotificationSummaryResponse::from);

        return NotificationListResponse.from(result);
    }
}
