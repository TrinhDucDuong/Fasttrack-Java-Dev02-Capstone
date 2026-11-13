package vn.edu.middlejava.learning.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.entity.CourseUnit;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;

import java.util.List;

@Configuration
public class CourseSeedConfiguration {
    @Bean
    ApplicationRunner seedCourse(ICourseRepository courses, ICourseUnitRepository units) {
        return args -> seed(courses, units);
    }

    @Transactional
    void seed(ICourseRepository courses, ICourseUnitRepository units) {
        Course course = courses.findBySlug("middle-java").orElseGet(() -> courses.save(
                new Course("middle-java", "Middle Java", "A practical learning path for Java backend engineering.")));
        if (units.existsByCourseIdAndUnitNumber(course.getId(), 1)) {
            return;
        }
        List<String[]> syllabus = List.of(
                new String[]{"SOLID Principles", "Clean Code; SRP; OCP; LSP; ISP; DIP"},
                new String[]{"Object-Oriented Programming and Refactoring", "Extract Method; Extract Class; Replace Conditional with Polymorphism"},
                new String[]{"Software Design Patterns", "Factory Method; Singleton; Observer; Strategy; Dependency Injection"},
                new String[]{"Database Tuning", "SQL optimization; Explain Plan; query analysis"},
                new String[]{"Database Indexing", "B-Tree; composite index; covering index; indexing best practices"},
                new String[]{"Database Connectivity", "Connection pooling principles and configuration"},
                new String[]{"Java SE", "Stream API; Optional; CompletableFuture; ExecutorService; Locks; JVM memory; Garbage Collection"},
                new String[]{"Java Hibernate", "N+1; EAGER and LAZY; batch size; first-level and second-level cache"},
                new String[]{"Spring Framework", "Transactions; @Transactional; propagation; isolation; transaction boundaries"},
                new String[]{"Spring Security", "JWT; refresh tokens; custom claims; OAuth2; OIDC; Authorization Code flow"},
                new String[]{"Spring Application Testing", "Unit and integration tests; MockMvc; @DataJpaTest; Testcontainers"},
                new String[]{"Spring Boot", "Actuator; Micrometer; Prometheus; @ConfigurationProperties; Profiles; @Scheduled"},
                new String[]{"Spring Cloud", "Microservices fundamentals; Spring Boot and Spring Cloud ecosystem"},
                new String[]{"Java Testing", "Testing Pyramid; JUnit; Mockito; integration testing; Testcontainers for database and message queue"}
        );
        for (int index = 0; index < syllabus.size(); index++) {
            String[] row = syllabus.get(index);
            units.save(new CourseUnit(course, index + 1, row[0], row[1]));
        }
    }
}
