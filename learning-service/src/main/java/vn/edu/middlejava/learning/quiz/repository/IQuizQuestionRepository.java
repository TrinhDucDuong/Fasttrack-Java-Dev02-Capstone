package vn.edu.middlejava.learning.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.quiz.entity.QuizQuestion;

public interface IQuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
}
