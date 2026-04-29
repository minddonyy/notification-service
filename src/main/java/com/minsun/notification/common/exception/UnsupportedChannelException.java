package com.minsun.notification.common.exception;

import com.minsun.notification.domain.NotificationChannel;

public class UnsupportedChannelException extends RuntimeException {
    public UnsupportedChannelException(NotificationChannel channel) {
        super("Unsupported channel: " + channel);
    }
}
