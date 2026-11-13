package vn.edu.middlejava.learning.progress.dto;

import java.util.List;

public record ProgressResponse(Long userId, int completedAttempts, int correctAnswers, int answeredQuestions,
                               double accuracyPercentage, List<AttemptSummary> recentAttempts) {
    public record AttemptSummary(Long quizId, int score, int total, java.time.Instant completedAt) {
    }
}
