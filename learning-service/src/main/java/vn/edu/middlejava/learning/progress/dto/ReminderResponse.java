package vn.edu.middlejava.learning.progress.dto;

import java.time.Instant;

public record ReminderResponse(Long id, Long unitId, Instant dueAt, Instant notifiedAt) {
}
