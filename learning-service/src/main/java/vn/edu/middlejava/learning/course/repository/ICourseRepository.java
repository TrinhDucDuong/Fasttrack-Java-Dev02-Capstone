package vn.edu.middlejava.learning.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.course.entity.Course;

import java.util.List;
import java.util.Optional;

public interface ICourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findBySlug(String slug);

    List<Course> findByActiveTrueOrderByIdAsc();

    /**
     * Generates {@code upper(title) like upper(?)} - served by the trigram index
     * idx_courses_title_substring (GIN on upper(title)) created in V2__course_store.sql.
     */
    List<Course> findByActiveTrueAndTitleContainingIgnoreCaseOrderByIdAsc(String title);
}
