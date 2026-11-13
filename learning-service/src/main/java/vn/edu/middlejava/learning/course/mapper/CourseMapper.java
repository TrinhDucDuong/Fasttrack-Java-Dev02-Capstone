package vn.edu.middlejava.learning.course.mapper;

import org.springframework.stereotype.Component;
import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.entity.CourseUnit;

@Component
public class CourseMapper {
    public CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getSlug(), course.getTitle(), course.getDescription());
    }

    public UnitResponse toResponse(CourseUnit unit) {
        return new UnitResponse(unit.getId(), unit.getCourse().getId(), unit.getUnitNumber(), unit.getTitle(), unit.getOutline());
    }
}
