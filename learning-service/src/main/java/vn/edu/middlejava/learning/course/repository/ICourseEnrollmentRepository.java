package vn.edu.middlejava.learning.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.course.entity.CourseEnrollment;

public interface ICourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
