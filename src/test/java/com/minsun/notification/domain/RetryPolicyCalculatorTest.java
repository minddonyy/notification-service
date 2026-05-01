package com.minsun.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RetryPolicyCalculatorTest {

    @Test
    @DisplayName("1회 실패 후 1분 뒤 재시도 (1² = 1)")
    void retryCount_1_1분뒤() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = RetryPolicyCalculator.nextRetryAt(1);
        assertThat(result).isBetween(before.plusMinutes(1), before.plusMinutes(1).plusSeconds(3));
    }

    @Test
    @DisplayName("2회 실패 후 4분 뒤 재시도 (2² = 4)")
    void retryCount_2_4분뒤() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = RetryPolicyCalculator.nextRetryAt(2);
        assertThat(result).isBetween(before.plusMinutes(4), before.plusMinutes(4).plusSeconds(3));
    }

    @Test
    @DisplayName("3회 실패 후 9분 뒤 재시도 (3² = 9)")
    void retryCount_3_9분뒤() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime result = RetryPolicyCalculator.nextRetryAt(3);
        assertThat(result).isBetween(before.plusMinutes(9), before.plusMinutes(9).plusSeconds(3));
    }
}
