package vn.edu.middlejava.learning.quiz.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.quiz.dto.AttemptResponse;
import vn.edu.middlejava.learning.quiz.dto.CreateQuizRequest;
import vn.edu.middlejava.learning.quiz.dto.GenerateQuizRequest;
import vn.edu.middlejava.learning.quiz.dto.QuizResponse;
import vn.edu.middlejava.learning.quiz.dto.SubmitAttemptRequest;
import vn.edu.middlejava.learning.quiz.service.IQuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final IQuizService quizService;

    public QuizController(IQuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<QuizResponse> create(@Valid @RequestBody CreateQuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.create(request));
    }

    @PostMapping("/generate")
    public ResponseEntity<QuizResponse> generate(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody GenerateQuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.generate(request, jwt.getTokenValue()));
    }

    @GetMapping("/units/{unitId}")
    public ResponseEntity<List<QuizResponse>> getForUnit(@PathVariable Long unitId) {
        return ResponseEntity.ok(quizService.getForUnit(unitId));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> get(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.get(quizId));
    }

    @PostMapping("/{quizId}/attempts")
    public ResponseEntity<AttemptResponse> submit(@PathVariable Long quizId, @AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody SubmitAttemptRequest request) {
        return ResponseEntity.ok(quizService.submit(Long.valueOf(jwt.getSubject()), quizId, request));
    }
}
