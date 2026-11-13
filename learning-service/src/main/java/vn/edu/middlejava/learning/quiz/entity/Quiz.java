package vn.edu.middlejava.learning.quiz.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuizQuestion> questions = new ArrayList<>();

    protected Quiz() {
    }

    public Quiz(Long unitId, String title, List<QuizQuestion> questions) {
        this.unitId = unitId;
        this.title = title;
        this.createdAt = Instant.now();
        questions.forEach(this::addQuestion);
    }

    private void addQuestion(QuizQuestion question) {
        question.setQuiz(this);
        questions.add(question);
    }

    public Long getId() { return id; }
    public Long getUnitId() { return unitId; }
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public List<QuizQuestion> getQuestions() { return questions; }
}
