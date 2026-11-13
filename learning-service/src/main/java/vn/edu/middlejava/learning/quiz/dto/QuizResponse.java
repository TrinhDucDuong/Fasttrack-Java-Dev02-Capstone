package vn.edu.middlejava.learning.quiz.dto;

import java.util.List;

public record QuizResponse(Long id, Long unitId, String title, List<QuestionResponse> questions) {
    public record QuestionResponse(Long id, String prompt, int order, List<OptionResponse> options) {
    }
    public record OptionResponse(Long id, String text) {
    }
}
