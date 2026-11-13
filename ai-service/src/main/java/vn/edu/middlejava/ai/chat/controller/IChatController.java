package vn.edu.middlejava.ai.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.edu.middlejava.ai.chat.dto.AnswerResponse;
import vn.edu.middlejava.ai.chat.dto.AskRequest;

public interface IChatController {
    ResponseEntity<AnswerResponse> ask(@AuthenticationPrincipal Jwt jwt, AskRequest request);
}
