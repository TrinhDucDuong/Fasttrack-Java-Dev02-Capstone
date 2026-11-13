package vn.edu.middlejava.learning.quiz.client;

import java.util.List;

public interface IAiQuizClient {
    List<GeneratedQuestion> generate(Long unitId, int questionCount, String bearerToken);

    record GeneratedQuestion(String prompt, String explanation, List<GeneratedOption> options) {
    }

    record GeneratedOption(String text, boolean correct) {
    }
}
