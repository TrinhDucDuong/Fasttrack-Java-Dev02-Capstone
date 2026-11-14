package vn.edu.middlejava.learning.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.service.ICommerceService;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.repository.ICourseEnrollmentRepository;

import java.util.UUID;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "server.port=0",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "app.admin.email=",
        "app.admin.password="
})
class PostgresCommerceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("study_coach")
            .withUsername("study_coach")
            .withPassword("study_coach");

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management")
            .withEnv("RABBITMQ_DEFAULT_USER", "study_coach")
            .withEnv("RABBITMQ_DEFAULT_PASS", "study_coach");

    @DynamicPropertySource
    static void localInfrastructure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "&currentSchema=learning,public");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "study_coach");
        registry.add("spring.rabbitmq.password", () -> "study_coach");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IStudyUserRepository users;

    @Autowired
    private ICourseRepository courses;

    @Autowired
    private ICommerceService commerceService;

    @Autowired
    private CustomerOrderRepository orders;

    @Autowired
    private ICourseEnrollmentRepository enrollments;

    @Test
    void flywayCreatesPostgresCommerceSchemaAndCheckoutIsAtomic() {
        Integer migrationCount = jdbcTemplate.queryForObject(
                "select count(*) from learning.flyway_schema_history "
                        + "where success = true and version in ('1', '2', '3', '4')", Integer.class);
        assertThat(migrationCount).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from pg_indexes where schemaname = 'learning' and indexname = 'idx_courses_title_substring'",
                Integer.class)).isEqualTo(1);

        StudyUser customer = users.save(new StudyUser("postgres-" + UUID.randomUUID() + "@example.test",
                "encoded-password"));
        Long courseId = courses.findBySlug("middle-java").orElseThrow().getId();
        commerceService.addToCart(customer.getId(), new AddCartItemRequest(courseId, 2));
        var order = commerceService.checkout(customer.getId(), new CheckoutRequest("SIMULATED"));

        assertThat(order.total()).isEqualByComparingTo("59.98");
        assertThat(order.paymentStatus()).isEqualTo("PAID");
        assertThat(commerceService.getCart(customer.getId()).items()).isEmpty();
        assertThat(orders.findByIdAndUserId(order.id(), customer.getId())).isPresent();
        assertThat(enrollments.existsByUserIdAndCourseId(customer.getId(), courseId)).isTrue();
    }
}
