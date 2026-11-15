package vn.edu.middlejava.learning.course.service.impl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.course.dto.CourseVideoResponse;
import vn.edu.middlejava.learning.course.entity.CourseEnrollment;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseEnrollmentRepository;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;
import vn.edu.middlejava.learning.course.video.IVideoStorage;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class CourseEnrollmentServiceImpl implements ICourseEnrollmentService {
    private final ICourseEnrollmentRepository enrollments;
    private final ICourseRepository courses;
    private final CourseMapper mapper;
    private final IVideoStorage storage;

    public CourseEnrollmentServiceImpl(ICourseEnrollmentRepository enrollments,
                                       ICourseRepository courses,
                                       CourseMapper mapper,
                                       IVideoStorage storage) {
        this.enrollments = enrollments;
        this.courses = courses;
        this.mapper = mapper;
        this.storage = storage;
    }

    @Override
    @Transactional
    public void enrollPaidCourses(Long userId, List<Long> courseIds) {
        courseIds.stream()
                .distinct()
                .filter(courseId -> !enrollments.existsByUserIdAndCourseId(userId, courseId))
                .map(courseId -> new CourseEnrollment(userId, courseId))
                .forEach(enrollments::save);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseVideoResponse getVideo(Long userId, Long courseId) {
        var course = courses.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (!enrollments.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AccessDeniedException("Purchase this course to access its video");
        }
        if (course.hasStoredVideo()) {
            IVideoStorage.PlaybackLink link = storage.presignPlayback(course.getVideoKey());
            return CourseVideoResponse.stored(course.getId(), course.getTitle(), link.url(),
                    course.getVideoContentType(), link.expiresAt());
        }
        return mapper.toVideoResponse(course);
    }
}
