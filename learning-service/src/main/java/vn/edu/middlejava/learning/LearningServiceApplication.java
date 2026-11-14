package vn.edu.middlejava.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableDiscoveryClient
@ConfigurationPropertiesScan
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class LearningServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
    }
}
