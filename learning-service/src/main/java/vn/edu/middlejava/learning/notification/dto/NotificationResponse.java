package vn.edu.middlejava.learning.notification.dto;

import java.time.Instant;

public record NotificationResponse(Long id, Long orderId, String message, Instant createdAt, boolean read) {
}
