package vn.edu.middlejava.ai.quiz.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.ai.quiz.dto.QuizDraftRequest;
import vn.edu.middlejava.ai.quiz.dto.QuizDraftResponse;
import vn.edu.middlejava.ai.quiz.service.IQuizDraftService;

@RestController
@RequestMapping("/api/ai/quiz-drafts")
public class QuizDraftController {
    private final IQuizDraftService quizDraftService;

    public QuizDraftController(IQuizDraftService quizDraftService) {
        this.quizDraftService = quizDraftService;
    }

    @PostMapping
    public ResponseEntity<QuizDraftResponse> generate(@Valid @RequestBody QuizDraftRequest request) {
        return ResponseEntity.ok(quizDraftService.generate(request));
    }
}
