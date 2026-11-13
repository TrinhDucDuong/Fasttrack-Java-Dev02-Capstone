package vn.edu.middlejava.learning.progress.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateReminderRequest(@NotNull Long unitId, @NotNull @Future Instant dueAt) {
}
