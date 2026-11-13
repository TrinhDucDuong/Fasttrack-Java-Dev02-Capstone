package vn.edu.middlejava.learning.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.middlejava.learning.quiz.entity.QuizAttempt;

import java.util.List;

public interface IQuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByCompletedAtDesc(Long userId);

    @Query("select coalesce(sum(a.score), 0), coalesce(sum(a.total), 0) from QuizAttempt a where a.userId = :userId")
    Object[] summarize(@Param("userId") Long userId);
}
