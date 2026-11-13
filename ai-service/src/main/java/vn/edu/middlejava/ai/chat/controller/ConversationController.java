package vn.edu.middlejava.ai.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.ai.chat.dto.ConversationResponse;
import vn.edu.middlejava.ai.chat.service.IChatService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final IChatService chatService;

    public ConversationController(IChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(@AuthenticationPrincipal Jwt jwt, @PathVariable Long conversationId) {
        return ResponseEntity.ok(chatService.getConversation(Long.valueOf(jwt.getSubject()), conversationId));
    }
}
