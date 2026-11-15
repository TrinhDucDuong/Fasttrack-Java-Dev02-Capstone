package vn.edu.middlejava.learning.course.video;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.video.dto.ConfirmVideoRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadRequest;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseVideoAdminServiceImplTest {
    @Mock
    private ICourseRepository courses;
    @Mock
    private IVideoStorage storage;

    private CourseVideoAdminServiceImpl service;
    private Course course;

    @BeforeEach
    void setUp() {
        VideoStorageProperties properties = new VideoStorageProperties("course-videos", "ap-southeast-2", null, null, null,
                false, false, Duration.ofMinutes(15), Duration.ofHours(1), 10_000_000L, List.of("video/mp4", "video/webm"));
        service = new CourseVideoAdminServiceImpl(courses, storage, properties, new CourseMapper());
        course = spy(new Course("middle-java", "Middle Java", "Course", new BigDecimal("29.99"),
                "https://www.youtube.com/watch?v=GoXwIVyNvX0"));
        org.mockito.Mockito.lenient().when(course.getId()).thenReturn(1L);
    }

    @Test
    void requestUploadReturnsPresignedUrlUnderTheCoursePrefix() {
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(storage.presignUpload(startsWith("courses/1/"), eq("video/mp4"), eq(2048L))).thenAnswer(call ->
                new IVideoStorage.UploadTicket(call.getArgument(0), "PUT", "https://storage/presigned",
                        Map.of("Content-Type", "video/mp4"), Instant.now().plusSeconds(900)));

        var ticket = service.requestUpload(1L, new VideoUploadRequest("lesson.mp4", "video/mp4", 2048));

        assertThat(ticket.key()).startsWith("courses/1/").endsWith(".mp4");
        assertThat(ticket.method()).isEqualTo("PUT");
        assertThat(ticket.headers()).containsEntry("Content-Type", "video/mp4");
    }

    @Test
    void requestUploadRejectsUnsupportedTypeAndOversizedFile() {
        when(courses.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.requestUpload(1L, new VideoUploadRequest("a.exe", "application/x-msdownload", 10)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.requestUpload(1L, new VideoUploadRequest("big.mp4", "video/mp4", 20_000_000)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(storage, never()).presignUpload(anyString(), anyString(), anyLong());
    }

    @Test
    void requestUploadForUnknownCourseIsNotFound() {
        when(courses.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestUpload(9L, new VideoUploadRequest("a.mp4", "video/mp4", 10)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmLinksTheUploadedObjectToTheCourse() {
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(storage.find("courses/1/abc.mp4"))
                .thenReturn(Optional.of(new IVideoStorage.StoredVideo("courses/1/abc.mp4", "video/mp4", 2048)));

        var response = service.confirmUpload(1L, new ConfirmVideoRequest("courses/1/abc.mp4"));

        assertThat(response.videoKey()).isEqualTo("courses/1/abc.mp4");
        assertThat(response.videoSizeBytes()).isEqualTo(2048L);
        assertThat(course.hasStoredVideo()).isTrue();
    }

    @Test
    void confirmRejectsKeyOfAnotherCourseAndMissingUpload() {
        when(courses.findById(1L)).thenReturn(Optional.of(course));
        when(storage.find(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmUpload(1L, new ConfirmVideoRequest("courses/2/abc.mp4")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.confirmUpload(1L, new ConfirmVideoRequest("courses/1/missing.mp4")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(course.hasStoredVideo()).isFalse();
    }
}
