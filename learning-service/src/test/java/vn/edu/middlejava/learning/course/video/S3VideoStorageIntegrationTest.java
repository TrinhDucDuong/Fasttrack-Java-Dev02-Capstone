package vn.edu.middlejava.learning.course.video;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test against a real S3-compatible server (SeaweedFS in Docker, with authentication on):
 * the same S3VideoStorage code the application uses on AWS.
 */
@Testcontainers(disabledWithoutDocker = true)
class S3VideoStorageIntegrationTest {
    @Container
    static final GenericContainer<?> s3 = new GenericContainer<>("chrislusf/seaweedfs:4.50")
            .withCopyFileToContainer(MountableFile.forClasspathResource("seaweedfs-s3.json"), "/etc/seaweedfs/s3.json")
            .withCommand("server", "-s3", "-s3.config=/etc/seaweedfs/s3.json", "-dir=/data")
            .withExposedPorts(8333)
            .waitingFor(Wait.forHttp("/").forPort(8333).forStatusCode(403))
            .withStartupTimeout(Duration.ofMinutes(2));

    private static final HttpClient http = HttpClient.newHttpClient();
    private static S3VideoStorage storage;
    private static S3VideoStorage shortLivedStorage;

    @BeforeAll
    static void connect() {
        storage = new S3VideoStorage(properties(Duration.ofHours(1)));
        shortLivedStorage = new S3VideoStorage(properties(Duration.ofSeconds(1)));
    }

    @AfterAll
    static void close() {
        storage.close();
        shortLivedStorage.close();
    }

    @Test
    void presignedUploadThenSignedPlaybackRoundTrip() throws Exception {
        byte[] video = "fake mp4 bytes for the demo".getBytes(StandardCharsets.UTF_8);
        String key = "courses/1/" + UUID.randomUUID() + ".mp4";

        var ticket = storage.presignUpload(key, "video/mp4", video.length);
        HttpResponse<String> upload = http.send(HttpRequest.newBuilder(URI.create(ticket.url()))
                .header("Content-Type", "video/mp4")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(video)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(upload.statusCode()).isEqualTo(200);

        var stored = storage.find(key).orElseThrow();
        assertThat(stored.sizeBytes()).isEqualTo(video.length);
        assertThat(stored.contentType()).isEqualTo("video/mp4");

        var link = storage.presignPlayback(key);
        HttpResponse<byte[]> playback = http.send(HttpRequest.newBuilder(URI.create(link.url())).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        assertThat(playback.statusCode()).isEqualTo(200);
        assertThat(playback.body()).isEqualTo(video);
    }

    @Test
    void storageRejectsUnsignedTamperedAndExpiredRequests() throws Exception {
        byte[] video = "secret lesson".getBytes(StandardCharsets.UTF_8);
        String key = "courses/2/" + UUID.randomUUID() + ".mp4";
        var ticket = storage.presignUpload(key, "video/mp4", video.length);
        http.send(HttpRequest.newBuilder(URI.create(ticket.url())).header("Content-Type", "video/mp4")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(video)).build(), HttpResponse.BodyHandlers.discarding());

        String signedUrl = storage.presignPlayback(key).url();
        String unsignedUrl = signedUrl.substring(0, signedUrl.indexOf('?'));
        String tamperedUrl = signedUrl.replaceAll("X-Amz-Signature=[0-9a-f]", "X-Amz-Signature=0");
        String expiringUrl = shortLivedStorage.presignPlayback(key).url();
        Thread.sleep(2500);

        for (String url : List.of(unsignedUrl, tamperedUrl, expiringUrl)) {
            int status = http.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode();
            assertThat(status).as(url).isEqualTo(403);
        }
    }

    @Test
    void findReturnsEmptyForMissingObject() {
        assertThat(storage.find("courses/9/missing.mp4")).isEmpty();
    }

    private static VideoStorageProperties properties(Duration playbackTtl) {
        String endpoint = "http://" + s3.getHost() + ":" + s3.getMappedPort(8333);
        return new VideoStorageProperties("course-videos-test", "ap-southeast-2", endpoint, "study_coach",
                "study_coach_local_secret", true, true, Duration.ofMinutes(15), playbackTtl, 10_000_000L,
                List.of("video/mp4"));
    }
}
