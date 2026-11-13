package vn.edu.middlejava.ai.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AskRequest(@NotNull Long unitId, Long conversationId, @NotBlank @Size(max = 2000) String question) {
}
