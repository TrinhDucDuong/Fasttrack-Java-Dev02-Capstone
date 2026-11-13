package vn.edu.middlejava.learning.quiz.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.quiz.entity.Quiz;

import java.util.List;
import java.util.Optional;

public interface IQuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(attributePaths = {"questions", "questions.options"})
    Optional<Quiz> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"questions", "questions.options"})
    List<Quiz> findDetailedByUnitIdOrderByCreatedAtDesc(Long unitId);
}
