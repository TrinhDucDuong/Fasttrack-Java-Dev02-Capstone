package vn.edu.middlejava.learning.quiz.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "text")
    private String prompt;

    @Column(nullable = false, columnDefinition = "text")
    private String explanation;

    @Column(name = "question_order", nullable = false)
    private int questionOrder;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id")
    private List<QuizOption> options = new ArrayList<>();

    protected QuizQuestion() {
    }

    public QuizQuestion(String prompt, String explanation, int questionOrder, List<QuizOption> options) {
        this.prompt = prompt;
        this.explanation = explanation;
        this.questionOrder = questionOrder;
        options.forEach(this::addOption);
    }

    void setQuiz(Quiz quiz) { this.quiz = quiz; }
    private void addOption(QuizOption option) {
        option.setQuestion(this);
        options.add(option);
    }
    public Long getId() { return id; }
    public String getPrompt() { return prompt; }
    public String getExplanation() { return explanation; }
    public int getQuestionOrder() { return questionOrder; }
    public List<QuizOption> getOptions() { return options; }
}
