package com.minsun.notification.application;

import com.minsun.notification.api.dto.NotificationCreateRequest;
import com.minsun.notification.api.dto.NotificationCreateResponse;
import com.minsun.notification.common.IdempotencyKeyGenerator;
import com.minsun.notification.common.exception.NotificationNotFoundException;
import com.minsun.notification.domain.Notification;
import com.minsun.notification.domain.NotificationChannel;
import com.minsun.notification.domain.NotificationType;
import com.minsun.notification.infrastructure.NotificationRepository;
import com.minsun.notification.infrastructure.event.NotificationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService service;

    @Mock NotificationRepository repository;
    @Mock NotificationWriter writer;
    @Mock IdempotencyKeyGenerator keyGenerator;
    @Mock NotificationEventPublisher eventPublisher;

    private final NotificationCreateRequest request = new NotificationCreateRequest(
            42L, NotificationType.ENROLLMENT_COMPLETE, "COURSE", 100L, NotificationChannel.EMAIL, null);

    @Test
    @DisplayName("신규 알림 등록 시 isNew=true 반환")
    void 신규_알림_등록시_isNew_true() {
        String key = "42:ENROLLMENT_COMPLETE:COURSE:100:EMAIL";
        given(keyGenerator.generate(any(), any(), any(), any(), any())).willReturn(key);
        Notification saved = Notification.create(key, 42L, NotificationType.ENROLLMENT_COMPLETE, "COURSE", 100L, NotificationChannel.EMAIL, null);
        given(writer.save(any())).willReturn(saved);

        NotificationCreateResponse result = service.register(request);

        assertThat(result.isNew()).isTrue();
        assertThat(result.idempotencyKey()).isEqualTo(key);
    }

    @Test
    @DisplayName("중복 요청 시 DataIntegrityViolationException → isNew=false, 기존 건 반환")
    void 중복_요청시_isNew_false() {
        String key = "42:ENROLLMENT_COMPLETE:COURSE:100:EMAIL";
        given(keyGenerator.generate(any(), any(), any(), any(), any())).willReturn(key);
        given(writer.save(any())).willThrow(DataIntegrityViolationException.class);
        Notification existing = Notification.create(key, 42L, NotificationType.ENROLLMENT_COMPLETE, "COURSE", 100L, NotificationChannel.EMAIL, null);
        given(repository.findByIdempotencyKey(key)).willReturn(Optional.of(existing));

        NotificationCreateResponse result = service.register(request);

        assertThat(result.isNew()).isFalse();
        assertThat(result.idempotencyKey()).isEqualTo(key);
    }

    @Test
    @DisplayName("존재하지 않는 알림 조회 시 NotificationNotFoundException 발생")
    void 존재하지않는_알림_조회시_예외() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
