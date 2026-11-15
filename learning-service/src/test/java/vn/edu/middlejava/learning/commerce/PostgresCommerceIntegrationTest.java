package vn.edu.middlejava.learning.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.service.IAdminOrderService;
import vn.edu.middlejava.learning.commerce.service.ICartService;
import vn.edu.middlejava.learning.commerce.service.IOrderService;
import vn.edu.middlejava.learning.course.repository.ICourseEnrollmentRepository;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;
import vn.edu.middlejava.learning.notification.repository.UserNotificationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

/**
 * Integration test of the ordering flow on a real PostgreSQL (Flyway migrations, indexes,
 * transactions) and RabbitMQ, both started by Testcontainers.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "server.port=0",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "app.admin.email=",
        "app.admin.password=",
        "app.video-storage.create-bucket=false"
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
    private ICartService cartService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IAdminOrderService adminOrderService;
    @Autowired
    private CustomerOrderRepository orders;
    @Autowired
    private ICourseEnrollmentRepository enrollments;
    @Autowired
    private UserNotificationRepository notifications;
    @MockitoSpyBean
    private ICourseEnrollmentService enrollmentService;

    @Test
    void flywayCreatesSchemaAndSearchIndex() {
        Integer migrationCount = jdbcTemplate.queryForObject(
                "select count(*) from learning.flyway_schema_history "
                        + "where success = true and version in ('1', '2', '3', '4', '5')", Integer.class);
        assertThat(migrationCount).isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from pg_indexes where schemaname = 'learning' and indexname = 'idx_courses_title_substring'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void vnpayCheckoutPersistsOrderUnlocksCourseAndNotifiesAsynchronously() throws Exception {
        StudyUser customer = newCustomer();
        Long courseId = courses.findBySlug("middle-java").orElseThrow().getId();

        cartService.addToCart(customer.getId(), new AddCartItemRequest(courseId, 2));
        var order = orderService.checkout(customer.getId(), new CheckoutRequest("VNPAY"));

        assertThat(order.total()).isEqualByComparingTo("59.98");
        assertThat(order.paymentStatus()).isEqualTo("PAID");
        assertThat(order.paymentReference()).startsWith("VNP");
        assertThat(cartService.getCart(customer.getId()).items()).isEmpty();
        assertThat(orders.findByIdAndUserId(order.id(), customer.getId())).isPresent();
        assertThat(enrollments.existsByUserIdAndCourseId(customer.getId(), courseId)).isTrue();
        awaitNotifications(customer.getId(), 1);
    }

    @Test
    void codOrderIsUnlockedOnlyAfterAdminConfirmsPayment() throws Exception {
        StudyUser customer = newCustomer();
        Long courseId = courses.findBySlug("java-fundamentals").orElseThrow().getId();
        cartService.addToCart(customer.getId(), new AddCartItemRequest(courseId, 1));

        var order = orderService.checkout(customer.getId(), new CheckoutRequest("COD"));
        assertThat(order.paymentStatus()).isEqualTo("PENDING");
        assertThat(enrollments.existsByUserIdAndCourseId(customer.getId(), courseId)).isFalse();

        var confirmed = adminOrderService.confirmPayment(order.id());
        assertThat(confirmed.status()).isEqualTo("CONFIRMED");
        assertThat(enrollments.existsByUserIdAndCourseId(customer.getId(), courseId)).isTrue();
        awaitNotifications(customer.getId(), 2);
    }

    @Test
    void transactionalCheckoutRollsBackEverythingWhenAStepFails() {
        StudyUser customer = newCustomer();
        Long courseId = courses.findBySlug("middle-java").orElseThrow().getId();
        cartService.addToCart(customer.getId(), new AddCartItemRequest(courseId, 1));
        long ordersBefore = orders.count();
        doThrow(new IllegalStateException("enrollment storage unavailable"))
                .when(enrollmentService).enrollPaidCourses(any(), anyList());

        try {
            assertThatThrownBy(() -> orderService.checkout(customer.getId(), new CheckoutRequest("VNPAY")))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            reset(enrollmentService);
        }

        assertThat(orders.count()).isEqualTo(ordersBefore);
        assertThat(cartService.getCart(customer.getId()).items()).hasSize(1);
        assertThat(notifications.findByUserIdOrderByCreatedAtDesc(customer.getId())).isEmpty();
    }

    private StudyUser newCustomer() {
        return users.save(new StudyUser("postgres-" + UUID.randomUUID() + "@example.test", "encoded-password"));
    }

    private void awaitNotifications(Long userId, int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (notifications.findByUserIdOrderByCreatedAtDesc(userId).size() < expected
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertThat(notifications.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(expected);
    }
}
