package vn.edu.middlejava.learning.course.video;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VideoStorageConfiguration {
    @Bean(destroyMethod = "close")
    S3VideoStorage videoStorage(VideoStorageProperties properties) {
        return new S3VideoStorage(properties);
    }
}
