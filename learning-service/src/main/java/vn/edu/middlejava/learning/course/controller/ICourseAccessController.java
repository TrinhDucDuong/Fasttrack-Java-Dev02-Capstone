package vn.edu.middlejava.learning.course.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import vn.edu.middlejava.learning.course.dto.CourseVideoResponse;

public interface ICourseAccessController {
    CourseVideoResponse getVideo(Jwt jwt, Long courseId);
}
