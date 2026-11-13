package vn.edu.middlejava.ai.quiz.service;

import vn.edu.middlejava.ai.quiz.dto.QuizDraftRequest;
import vn.edu.middlejava.ai.quiz.dto.QuizDraftResponse;

public interface IQuizDraftService {
    QuizDraftResponse generate(QuizDraftRequest request);
}
