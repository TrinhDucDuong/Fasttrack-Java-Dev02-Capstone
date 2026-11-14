package vn.edu.middlejava.learning.course.service;

import vn.edu.middlejava.learning.course.dto.CourseVideoResponse;

import java.util.List;

public interface ICourseEnrollmentService {
    void enrollPaidCourses(Long userId, List<Long> courseIds);

    CourseVideoResponse getVideo(Long userId, Long courseId);
}
