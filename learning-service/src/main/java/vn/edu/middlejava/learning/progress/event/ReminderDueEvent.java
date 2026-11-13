package vn.edu.middlejava.learning.progress.event;

import java.time.Instant;

public record ReminderDueEvent(Long userId, Long unitId, Instant dueAt) {
}
