package vn.edu.middlejava.learning.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuizRequest(@NotNull Long unitId, @NotBlank @Size(max = 180) String title,
                                @NotEmpty @Size(max = 20) List<@Valid QuestionRequest> questions) {
    public record QuestionRequest(@NotBlank String prompt, @NotBlank String explanation,
                                  @NotEmpty @Size(min = 2, max = 6) List<@Valid OptionRequest> options) {
    }
    public record OptionRequest(@NotBlank String text, boolean correct) {
    }
}
