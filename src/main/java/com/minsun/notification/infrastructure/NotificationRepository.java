package com.minsun.notification.infrastructure;

import com.minsun.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    // PENDING 건 폴링 — FOR UPDATE SKIP LOCKED으로 다중 인스턴스 중복 처리 방지
    @Query(value = """
            SELECT * FROM notifications
            WHERE status = 'PENDING'
              AND scheduled_at <= NOW()
            ORDER BY created_at
            LIMIT 50
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Notification> findPendingForProcessing();

    // FAILED 건 중 next_retry_at이 지난 재시도 대상
    @Query(value = """
            SELECT * FROM notifications
            WHERE status = 'FAILED'
              AND next_retry_at <= NOW()
            ORDER BY created_at
            LIMIT 50
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Notification> findFailedForRetry();

    // PROCESSING 상태가 10분 이상 지속된 stuck 건 → PENDING 복구
    @Modifying
    @Query(value = """
            UPDATE notifications
            SET status = 'PENDING', updated_at = NOW()
            WHERE status = 'PROCESSING'
              AND updated_at < NOW() - INTERVAL '10 minutes'
            """, nativeQuery = true)
    int recoverStuckProcessing();

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndIsReadOrderByCreatedAtDesc(Long recipientId, boolean isRead, Pageable pageable);
}
