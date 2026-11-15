package vn.edu.middlejava.learning.course.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseEnrollmentRepository;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.video.IVideoStorage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseEnrollmentServiceImplTest {
    private final ICourseEnrollmentRepository enrollments = mock(ICourseEnrollmentRepository.class);
    private final ICourseRepository courses = mock(ICourseRepository.class);
    private final IVideoStorage storage = mock(IVideoStorage.class);
    private final CourseEnrollmentServiceImpl service =
            new CourseEnrollmentServiceImpl(enrollments, courses, new CourseMapper(), storage);

    @Test
    void enrolledCustomerGetsShortLivedStorageUrlForUploadedVideo() {
        Course course = course();
        course.attachVideo("courses/1/lesson.mp4", "video/mp4", 2048);
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(enrollments.existsByUserIdAndCourseId(7L, 1L)).thenReturn(true);
        Instant expiresAt = Instant.now().plusSeconds(3600);
        when(storage.presignPlayback("courses/1/lesson.mp4"))
                .thenReturn(new IVideoStorage.PlaybackLink("https://storage/signed", expiresAt));

        var video = service.getVideo(7L, 1L);

        assertThat(video.source()).isEqualTo("STORAGE");
        assertThat(video.videoUrl()).isEqualTo("https://storage/signed");
        assertThat(video.expiresAt()).isEqualTo(expiresAt);
        assertThat(video.contentType()).isEqualTo("video/mp4");
    }

    @Test
    void courseWithoutUploadFallsBackToYoutube() {
        when(courses.findById(1L)).thenReturn(Optional.of(course()));
        when(enrollments.existsByUserIdAndCourseId(7L, 1L)).thenReturn(true);

        var video = service.getVideo(7L, 1L);

        assertThat(video.source()).isEqualTo("YOUTUBE");
        assertThat(video.youtubeEmbedUrl()).isEqualTo("https://www.youtube.com/embed/GoXwIVyNvX0");
        verify(storage, never()).presignPlayback(any());
    }

    @Test
    void customerWhoHasNotPaidNeverReceivesASignedUrl() {
        Course course = course();
        course.attachVideo("courses/1/lesson.mp4", "video/mp4", 2048);
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(enrollments.existsByUserIdAndCourseId(7L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.getVideo(7L, 1L)).isInstanceOf(AccessDeniedException.class);
        verify(storage, never()).presignPlayback(any());
    }

    private Course course() {
        return new Course("middle-java", "Middle Java", "Course", new BigDecimal("29.99"),
                "https://www.youtube.com/watch?v=GoXwIVyNvX0");
    }
}
