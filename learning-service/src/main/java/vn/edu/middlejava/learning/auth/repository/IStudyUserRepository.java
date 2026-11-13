package vn.edu.middlejava.learning.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.auth.entity.StudyUser;

import java.util.Optional;

public interface IStudyUserRepository extends JpaRepository<StudyUser, Long> {
    Optional<StudyUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
