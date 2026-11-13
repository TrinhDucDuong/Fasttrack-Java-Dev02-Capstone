package vn.edu.middlejava.learning.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.middlejava.learning.course.entity.CourseUnit;

import java.util.List;
import java.util.Optional;

public interface ICourseUnitRepository extends JpaRepository<CourseUnit, Long> {
    List<CourseUnit> findByCourseIdOrderByUnitNumber(Long courseId);
    Optional<CourseUnit> findByIdAndCourseId(Long id, Long courseId);
    boolean existsByCourseIdAndUnitNumber(Long courseId, int unitNumber);
}
