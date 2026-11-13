package vn.edu.middlejava.learning.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int total;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected QuizAttempt() {
    }

    public QuizAttempt(Long userId, Long quizId, int score, int total) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.total = total;
        this.completedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getQuizId() { return quizId; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public Instant getCompletedAt() { return completedAt; }
}
