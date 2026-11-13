package vn.edu.middlejava.ai.provider;

import java.util.List;

public record QuizDraftQuestion(String prompt, String explanation, List<Option> options) {
    public record Option(String text, boolean correct) {
    }
}
