package vn.edu.middlejava.learning.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.course.entity.Course;

import java.util.Optional;

public interface ICourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);
}
