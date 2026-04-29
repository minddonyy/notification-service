package com.minsun.notification.common;

import com.minsun.notification.common.exception.InvalidNotificationStatusException;
import com.minsun.notification.common.exception.NotificationNotFoundException;
import com.minsun.notification.common.exception.UnsupportedChannelException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotificationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({UnsupportedChannelException.class, InvalidNotificationStatusException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }
}
