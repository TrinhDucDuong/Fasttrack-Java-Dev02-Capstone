package vn.edu.middlejava.learning.course.service;

import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;

import java.util.List;

public interface ICourseService {
    List<CourseResponse> getCourses();
    List<UnitResponse> getUnits(Long courseId);
    UnitResponse getUnit(Long courseId, Long unitId);
}
