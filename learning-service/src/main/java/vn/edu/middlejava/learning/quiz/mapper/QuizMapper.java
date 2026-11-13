package vn.edu.middlejava.learning.quiz.mapper;

import org.springframework.stereotype.Component;
import vn.edu.middlejava.learning.quiz.dto.QuizResponse;
import vn.edu.middlejava.learning.quiz.entity.Quiz;

@Component
public class QuizMapper {
    public QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(quiz.getId(), quiz.getUnitId(), quiz.getTitle(), quiz.getQuestions().stream()
                .sorted(java.util.Comparator.comparingInt(question -> question.getQuestionOrder()))
                .map(question -> new QuizResponse.QuestionResponse(question.getId(), question.getPrompt(), question.getQuestionOrder(),
                        question.getOptions().stream().map(option -> new QuizResponse.OptionResponse(option.getId(), option.getText())).toList()))
                .toList());
    }
}
