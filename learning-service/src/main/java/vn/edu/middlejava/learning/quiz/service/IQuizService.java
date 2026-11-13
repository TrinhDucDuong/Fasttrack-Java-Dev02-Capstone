package vn.edu.middlejava.learning.quiz.service;

import vn.edu.middlejava.learning.quiz.dto.AttemptResponse;
import vn.edu.middlejava.learning.quiz.dto.CreateQuizRequest;
import vn.edu.middlejava.learning.quiz.dto.GenerateQuizRequest;
import vn.edu.middlejava.learning.quiz.dto.QuizResponse;
import vn.edu.middlejava.learning.quiz.dto.SubmitAttemptRequest;

import java.util.List;

public interface IQuizService {
    QuizResponse create(CreateQuizRequest request);
    QuizResponse generate(GenerateQuizRequest request, String bearerToken);
    QuizResponse get(Long quizId);
    List<QuizResponse> getForUnit(Long unitId);
    AttemptResponse submit(Long userId, Long quizId, SubmitAttemptRequest request);
}
