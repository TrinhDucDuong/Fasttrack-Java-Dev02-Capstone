package vn.edu.middlejava.learning.course.video;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Object storage settings. Local: an S3-compatible server (SeaweedFS) with an explicit endpoint and keys.
 * AWS: real S3, endpoint and keys left blank so the SDK uses the EC2 instance role.
 */
@ConfigurationProperties("app.video-storage")
public record VideoStorageProperties(
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        boolean pathStyle,
        boolean createBucket,
        Duration uploadUrlTtl,
        Duration playbackUrlTtl,
        long maxUploadBytes,
        List<String> allowedContentTypes) {

    public VideoStorageProperties {
        region = isBlank(region) ? "ap-southeast-2" : region;
        uploadUrlTtl = uploadUrlTtl == null ? Duration.ofMinutes(15) : uploadUrlTtl;
        playbackUrlTtl = playbackUrlTtl == null ? Duration.ofHours(1) : playbackUrlTtl;
        maxUploadBytes = maxUploadBytes <= 0 ? 500L * 1024 * 1024 : maxUploadBytes;
        allowedContentTypes = allowedContentTypes == null || allowedContentTypes.isEmpty()
                ? List.of("video/mp4", "video/webm", "video/quicktime") : List.copyOf(allowedContentTypes);
    }

    public boolean hasCustomEndpoint() {
        return !isBlank(endpoint);
    }

    public boolean hasStaticCredentials() {
        return !isBlank(accessKey) && !isBlank(secretKey);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
