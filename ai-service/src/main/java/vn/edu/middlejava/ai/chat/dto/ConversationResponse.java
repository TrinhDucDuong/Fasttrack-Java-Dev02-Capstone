package vn.edu.middlejava.ai.chat.dto;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(Long id, Long unitId, Instant createdAt, List<Message> messages) {
    public record Message(String role, String content, String model, Instant createdAt) {
    }
}
