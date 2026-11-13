package vn.edu.middlejava.learning.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateQuizRequest(@NotNull Long unitId, @Min(1) @Max(10) int questionCount) {
}
