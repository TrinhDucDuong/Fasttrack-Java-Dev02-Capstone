package vn.edu.middlejava.ai.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record QuizDraftRequest(@NotNull Long unitId, @Min(1) @Max(10) int questionCount) {
}
