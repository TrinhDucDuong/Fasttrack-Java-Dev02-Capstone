package vn.edu.middlejava.learning.course.controller;

import org.springframework.http.ResponseEntity;
import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;

import java.util.List;

public interface ICourseController {
    ResponseEntity<List<CourseResponse>> getCourses();
    ResponseEntity<List<UnitResponse>> getUnits(Long courseId);
    ResponseEntity<UnitResponse> getUnit(Long courseId, Long unitId);
}
