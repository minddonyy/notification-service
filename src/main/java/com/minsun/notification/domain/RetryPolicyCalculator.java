package com.minsun.notification.domain;

import java.time.LocalDateTime;

public class RetryPolicyCalculator {

    // delay(분) = retryCount²  →  1회 실패: 1분, 2회: 4분, 3회: 9분
    public static LocalDateTime nextRetryAt(int retryCount) {
        return LocalDateTime.now().plusMinutes((long) retryCount * retryCount);
    }

    private RetryPolicyCalculator() {}
}
