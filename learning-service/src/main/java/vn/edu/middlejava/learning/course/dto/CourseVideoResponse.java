package vn.edu.middlejava.learning.course.dto;

import java.time.Instant;

/**
 * source = STORAGE: videoUrl is a short-lived signed URL to the uploaded file (play with a video tag).
 * source = YOUTUBE: fallback for courses without an upload; videoUrl is the embed URL.
 */
public record CourseVideoResponse(Long courseId, String title, String source, String videoUrl,
                                  String contentType, Instant expiresAt, String youtubeEmbedUrl) {

    public static CourseVideoResponse stored(Long courseId, String title, String url, String contentType, Instant expiresAt) {
        return new CourseVideoResponse(courseId, title, "STORAGE", url, contentType, expiresAt, null);
    }

    public static CourseVideoResponse youtube(Long courseId, String title, String embedUrl) {
        return new CourseVideoResponse(courseId, title, "YOUTUBE", embedUrl, null, null, embedUrl);
    }
}
