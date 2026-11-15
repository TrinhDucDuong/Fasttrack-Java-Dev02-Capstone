package vn.edu.middlejava.learning.course.video;

import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;
import vn.edu.middlejava.learning.course.video.dto.ConfirmVideoRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadRequest;
import vn.edu.middlejava.learning.course.video.dto.VideoUploadResponse;

public interface ICourseVideoAdminService {
    VideoUploadResponse requestUpload(Long courseId, VideoUploadRequest request);

    CourseAdminResponse confirmUpload(Long courseId, ConfirmVideoRequest request);
}
