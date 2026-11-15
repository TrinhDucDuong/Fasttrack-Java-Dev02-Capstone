package vn.edu.middlejava.learning.course.video.dto;

import java.time.Instant;
import java.util.Map;

/** Where and how the client uploads the file directly to object storage. */
public record VideoUploadResponse(String key, String method, String uploadUrl, Map<String, String> headers,
                                  Instant expiresAt) {
}
