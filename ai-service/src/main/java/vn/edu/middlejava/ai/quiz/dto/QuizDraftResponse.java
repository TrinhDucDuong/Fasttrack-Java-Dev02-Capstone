package vn.edu.middlejava.ai.quiz.dto;

import java.util.List;

public record QuizDraftResponse(List<Question> questions) {
    public record Question(String prompt, String explanation, List<Option> options) {
    }
    public record Option(String text, boolean correct) {
    }
}
