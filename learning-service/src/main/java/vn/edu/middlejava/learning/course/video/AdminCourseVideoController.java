package vn.edu.middlejava.learning.course.video;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;
import vn.edu.middlejava.learning.course.video.dto.ConfirmVideoRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadResponse;

/** ADMIN only (/api/admin/**): upload a course video straight to object storage. */
@RestController
@RequestMapping("/api/admin/courses/{courseId}/video")
public class AdminCourseVideoController {
    private final ICourseVideoAdminService videos;

    public AdminCourseVideoController(ICourseVideoAdminService videos) {
        this.videos = videos;
    }

    /** Step 1: get a presigned PUT URL (valid for a few minutes). */
    @PostMapping("/upload-url")
    public VideoUploadResponse requestUpload(@PathVariable("courseId") Long courseId,
                                             @Valid @RequestBody VideoUploadRequest request) {
        return videos.requestUpload(courseId, request);
    }

    /** Step 3 (after the client PUT the file): link the uploaded object to the course. */
    @PutMapping
    public CourseAdminResponse confirmUpload(@PathVariable("courseId") Long courseId,
                                             @Valid @RequestBody ConfirmVideoRequest request) {
        return videos.confirmUpload(courseId, request);
    }
}
