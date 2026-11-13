package vn.edu.middlejava.ai.provider;

import java.util.List;

public interface IChatProvider {
    String answer(String question, List<String> context);
    java.util.List<QuizDraftQuestion> generateQuiz(java.util.List<String> context, int count);
    String modelName();
}
