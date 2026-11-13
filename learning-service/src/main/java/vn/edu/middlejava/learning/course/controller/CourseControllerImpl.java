package vn.edu.middlejava.learning.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;
import vn.edu.middlejava.learning.course.service.ICourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseControllerImpl implements ICourseController {
    private final ICourseService courseService;

    public CourseControllerImpl(ICourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses() {
        return ResponseEntity.ok(courseService.getCourses());
    }

    @Override
    @GetMapping("/{courseId}/units")
    public ResponseEntity<List<UnitResponse>> getUnits(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getUnits(courseId));
    }

    @Override
    @GetMapping("/{courseId}/units/{unitId}")
    public ResponseEntity<UnitResponse> getUnit(@PathVariable Long courseId, @PathVariable Long unitId) {
        return ResponseEntity.ok(courseService.getUnit(courseId, unitId));
    }
}
