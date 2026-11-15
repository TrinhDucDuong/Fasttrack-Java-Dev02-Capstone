package vn.edu.middlejava.learning.course.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

/** S3 implementation; the same code talks to SeaweedFS locally and to Amazon S3 on AWS. */
public class S3VideoStorage implements IVideoStorage, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(S3VideoStorage.class);

    private final VideoStorageProperties properties;
    private final S3Client client;
    private final S3Presigner presigner;

    public S3VideoStorage(VideoStorageProperties properties) {
        this.properties = properties;
        Region region = Region.of(properties.region());
        AwsCredentialsProvider credentials = properties.hasStaticCredentials()
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()))
                : DefaultCredentialsProvider.builder().build();
        S3Configuration s3Config = S3Configuration.builder().pathStyleAccessEnabled(properties.pathStyle()).build();

        var clientBuilder = S3Client.builder().region(region).credentialsProvider(credentials).serviceConfiguration(s3Config);
        var presignerBuilder = S3Presigner.builder().region(region).credentialsProvider(credentials).serviceConfiguration(s3Config);
        if (properties.hasCustomEndpoint()) {
            clientBuilder.endpointOverride(URI.create(properties.endpoint()));
            presignerBuilder.endpointOverride(URI.create(properties.endpoint()));
        }
        this.client = clientBuilder.build();
        this.presigner = presignerBuilder.build();
        if (properties.createBucket()) {
            ensureBucket();
        }
    }

    @Override
    public UploadTicket presignUpload(String key, String contentType, long sizeBytes) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength(sizeBytes)
                .build();
        PresignedPutObjectRequest presigned = presigner.presignPutObject(request -> request
                .signatureDuration(properties.uploadUrlTtl())
                .putObjectRequest(put));
        return new UploadTicket(key, "PUT", presigned.url().toString(), Map.of("Content-Type", contentType),
                presigned.expiration());
    }

    @Override
    public Optional<StoredVideo> find(String key) {
        try {
            HeadObjectResponse head = client.headObject(request -> request.bucket(properties.bucket()).key(key));
            return Optional.of(new StoredVideo(key, head.contentType(), head.contentLength()));
        } catch (NoSuchKeyException missing) {
            return Optional.empty();
        } catch (S3Exception error) {
            if (error.statusCode() == 404) {
                return Optional.empty();
            }
            throw error;
        }
    }

    @Override
    public PlaybackLink presignPlayback(String key) {
        GetObjectRequest get = GetObjectRequest.builder().bucket(properties.bucket()).key(key).build();
        PresignedGetObjectRequest presigned = presigner.presignGetObject(request -> request
                .signatureDuration(properties.playbackUrlTtl())
                .getObjectRequest(get));
        return new PlaybackLink(presigned.url().toString(), presigned.expiration());
    }

    /** Local convenience only: create the bucket on startup. Never fails application startup. */
    private void ensureBucket() {
        try {
            client.headBucket(request -> request.bucket(properties.bucket()));
        } catch (S3Exception notFound) {
            try {
                client.createBucket(request -> request.bucket(properties.bucket()));
                log.info("Created video bucket {}", properties.bucket());
            } catch (RuntimeException error) {
                log.warn("Could not create video bucket {}: {}", properties.bucket(), error.getMessage());
            }
        } catch (RuntimeException unavailable) {
            log.warn("Video storage {} is not reachable yet: {}", properties.endpoint(), unavailable.getMessage());
        }
    }

    @Override
    public void close() {
        presigner.close();
        client.close();
    }
}
