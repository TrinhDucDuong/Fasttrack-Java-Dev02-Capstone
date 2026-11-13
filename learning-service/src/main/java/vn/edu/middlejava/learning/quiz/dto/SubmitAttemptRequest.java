package vn.edu.middlejava.learning.quiz.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitAttemptRequest(@NotEmpty Map<@NotNull Long, @NotNull Long> answers) {
}
