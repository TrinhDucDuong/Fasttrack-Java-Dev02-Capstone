package vn.edu.middlejava.ai.chat.dto;

import java.util.List;

public record AnswerResponse(Long conversationId, String answer, String model, List<SourceResponse> sources) {
}
