package vn.edu.middlejava.learning.course.video;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.video.dto.ConfirmVideoRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadResponse;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * SRP: only manages course video files. Two steps so the bytes go straight from the client to storage:
 * 1) requestUpload validates the file and returns a presigned PUT URL;
 * 2) confirmUpload checks the object really exists before linking it to the course.
 */
@Service
public class CourseVideoAdminServiceImpl implements ICourseVideoAdminService {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "video/mp4", ".mp4", "video/webm", ".webm", "video/quicktime", ".mov");

    private final ICourseRepository courses;
    private final IVideoStorage storage;
    private final VideoStorageProperties properties;
    private final CourseMapper mapper;

    public CourseVideoAdminServiceImpl(ICourseRepository courses, IVideoStorage storage,
                                       VideoStorageProperties properties, CourseMapper mapper) {
        this.courses = courses;
        this.storage = storage;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public VideoUploadResponse requestUpload(Long courseId, VideoUploadRequest request) {
        Course course = findCourse(courseId);
        String contentType = normalize(request.contentType());
        validate(contentType, request.sizeBytes());
        String key = keyPrefix(course.getId()) + UUID.randomUUID() + EXTENSIONS.getOrDefault(contentType, "");
        IVideoStorage.UploadTicket ticket = storage.presignUpload(key, contentType, request.sizeBytes());
        return new VideoUploadResponse(ticket.key(), ticket.method(), ticket.url(), ticket.headers(), ticket.expiresAt());
    }

    @Override
    @Transactional
    public CourseAdminResponse confirmUpload(Long courseId, ConfirmVideoRequest request) {
        Course course = findCourse(courseId);
        if (!request.key().startsWith(keyPrefix(course.getId()))) {
            throw new IllegalArgumentException("Video key does not belong to this course");
        }
        IVideoStorage.StoredVideo stored = storage.find(request.key())
                .orElseThrow(() -> new IllegalStateException("Video has not been uploaded yet"));
        validate(normalize(stored.contentType()), stored.sizeBytes());
        course.attachVideo(stored.key(), normalize(stored.contentType()), stored.sizeBytes());
        return mapper.toAdminResponse(course);
    }

    private void validate(String contentType, long sizeBytes) {
        if (!properties.allowedContentTypes().contains(contentType)) {
            throw new IllegalArgumentException("Only " + properties.allowedContentTypes() + " videos are accepted");
        }
        if (sizeBytes <= 0 || sizeBytes > properties.maxUploadBytes()) {
            throw new IllegalArgumentException("Video must be between 1 byte and " + properties.maxUploadBytes() + " bytes");
        }
    }

    private Course findCourse(Long courseId) {
        return courses.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private static String keyPrefix(Long courseId) {
        return "courses/" + courseId + "/";
    }

    private static String normalize(String contentType) {
        return contentType == null ? "" : contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }
}
