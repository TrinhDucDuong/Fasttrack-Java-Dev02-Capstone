package vn.edu.middlejava.ai.chat.service;

import vn.edu.middlejava.ai.chat.dto.AnswerResponse;
import vn.edu.middlejava.ai.chat.dto.AskRequest;
import vn.edu.middlejava.ai.chat.dto.ConversationResponse;

public interface IChatService {
    AnswerResponse ask(Long userId, AskRequest request);
    ConversationResponse getConversation(Long userId, Long conversationId);
}
