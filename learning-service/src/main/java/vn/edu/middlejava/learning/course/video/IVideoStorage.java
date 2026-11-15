package vn.edu.middlejava.learning.course.video;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Abstraction over object storage (Dependency Inversion): services never touch the S3 SDK.
 * Video bytes never go through the application; clients upload and play through short-lived URLs.
 */
public interface IVideoStorage {

    UploadTicket presignUpload(String key, String contentType, long sizeBytes);

    Optional<StoredVideo> find(String key);

    PlaybackLink presignPlayback(String key);

    record UploadTicket(String key, String method, String url, Map<String, String> headers, Instant expiresAt) {
    }

    record StoredVideo(String key, String contentType, long sizeBytes) {
    }

    record PlaybackLink(String url, Instant expiresAt) {
    }
}
