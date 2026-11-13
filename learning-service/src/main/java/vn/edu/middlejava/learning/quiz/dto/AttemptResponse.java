package vn.edu.middlejava.learning.quiz.dto;

import java.time.Instant;

public record AttemptResponse(Long attemptId, Long quizId, int score, int total, double percentage, Instant completedAt) {
}
