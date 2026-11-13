package vn.edu.middlejava.ai.chat.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.edu.middlejava.ai.chat.dto.AnswerResponse;
import vn.edu.middlejava.ai.chat.dto.AskRequest;
import vn.edu.middlejava.ai.chat.service.IChatService;

@RestController
@RequestMapping("/api/ai")
public class ChatControllerImpl implements IChatController {
    private final IChatService chatService;

    public ChatControllerImpl(IChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    @PostMapping("/ask")
    public ResponseEntity<AnswerResponse> ask(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(chatService.ask(Long.valueOf(jwt.getSubject()), request));
    }

}
