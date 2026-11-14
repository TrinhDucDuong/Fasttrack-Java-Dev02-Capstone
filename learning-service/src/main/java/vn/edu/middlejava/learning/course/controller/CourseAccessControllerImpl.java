package vn.edu.middlejava.learning.course.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.course.dto.CourseVideoResponse;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;

@RestController
@RequestMapping("/api/courses")
public class CourseAccessControllerImpl implements ICourseAccessController {
    private final ICourseEnrollmentService enrollments;

    public CourseAccessControllerImpl(ICourseEnrollmentService enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    @GetMapping("/{courseId}/video")
    public CourseVideoResponse getVideo(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable("courseId") Long courseId) {
        return enrollments.getVideo(Long.valueOf(jwt.getSubject()), courseId);
    }
}
