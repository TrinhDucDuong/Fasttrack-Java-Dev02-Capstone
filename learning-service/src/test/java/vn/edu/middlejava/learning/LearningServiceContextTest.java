package vn.edu.middlejava.learning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.middlejava.learning.auth.entity.StudyUser;
import vn.edu.middlejava.learning.auth.repository.IStudyUserRepository;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.service.ICartService;
import vn.edu.middlejava.learning.commerce.service.IOrderService;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseEnrollmentRepository;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;
import vn.edu.middlejava.learning.notification.repository.UserNotificationRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LearningServiceContextTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICourseUnitRepository unitRepository;

    @Autowired
    private IStudyUserRepository userRepository;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Autowired
    private ICourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private UserNotificationRepository notificationRepository;

    @Test
    void startsAndSeedsTheCourseOutline() {
        Long courseId = courseRepository.findBySlug("middle-java").orElseThrow().getId();

        assertThat(unitRepository.findByCourseIdOrderByUnitNumber(courseId)).hasSize(14);
    }

    @Test
    @Transactional
    void checkoutPersistsOrderSnapshotAndEmptiesCart() {
        StudyUser user = userRepository.save(new StudyUser(
                "checkout-" + UUID.randomUUID() + "@example.test", "encoded-password"));
        Course course = courseRepository.save(new Course("integration-" + UUID.randomUUID(),
                "Integration Java Course", "Integration-test product", new BigDecimal("25.50"),
                "https://www.youtube.com/watch?v=GoXwIVyNvX0"));

        cartService.addToCart(user.getId(), new AddCartItemRequest(course.getId(), 2));
        var order = orderService.checkout(user.getId(), new CheckoutRequest("VNPAY"));

        assertThat(order.total()).isEqualByComparingTo("51.00");
        assertThat(order.paymentStatus()).isEqualTo("PAID");
        assertThat(order.items()).singleElement().satisfies(item -> {
            assertThat(item.courseTitle()).isEqualTo("Integration Java Course");
            assertThat(item.price()).isEqualByComparingTo("25.50");
        });
        assertThat(cartService.getCart(user.getId()).items()).isEmpty();
        assertThat(orderService.getOrder(user.getId(), order.id()).total()).isEqualByComparingTo("51.00");
        assertThat(enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())).isTrue();
    }

    @Test
    void customerCanRegisterManageCartCheckoutAndReadNotificationWhileAdminOwnsCatalog() throws Exception {
        String customerEmail = "customer-" + UUID.randomUUID() + "@example.test";
        String customerToken = registerAndGetToken(customerEmail, "CustomerPass123!");
        Long customerId = userRepository.findByEmail(customerEmail).orElseThrow().getId();
        Course course = courseRepository.findBySlug("middle-java").orElseThrow();

        mockMvc.perform(get("/api/courses").param("search", "middle java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(29.99))
                .andExpect(jsonPath("$[0].youtubeUrl").doesNotExist());

        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + course.getId() + ",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(1));

        mockMvc.perform(put("/api/cart/items/{courseId}", course.getId())
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(59.98));

        mockMvc.perform(delete("/api/cart/items/{courseId}", course.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/courses/{courseId}/video", course.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + course.getId() + ",\"quantity\":2}"))
                .andExpect(status().isOk());

        JsonNode paidOrder = objectMapper.readTree(mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"VNPAY\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(59.98))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn().getResponse().getContentAsString());
        long paidOrderId = paidOrder.path("id").asLong();

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        mockMvc.perform(get("/api/orders/{id}", paidOrderId).header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].courseTitle").value("Middle Java"));
        mockMvc.perform(get("/api/courses/{courseId}/video", course.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.youtubeEmbedUrl").value("https://www.youtube.com/embed/GoXwIVyNvX0"));

        Course pendingCourse = courseRepository.findBySlug("java-fundamentals").orElseThrow();
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + pendingCourse.getId() + ",\"quantity\":1}"))
                .andExpect(status().isOk());
        JsonNode codOrder = objectMapper.readTree(mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"COD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
                .andReturn().getResponse().getContentAsString());
        long codOrderId = codOrder.path("id").asLong();
        mockMvc.perform(get("/api/courses/{courseId}/video", pendingCourse.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/admin/orders/{id}/confirm-payment", codOrderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        StudyUser admin = userRepository.save(new StudyUser("admin-" + UUID.randomUUID() + "@example.test",
                passwordEncoder.encode("AdminPass123!"), "ADMIN"));
        String adminToken = loginAndGetToken(admin.getEmail(), "AdminPass123!");
        mockMvc.perform(put("/api/admin/orders/{id}/confirm-payment", codOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        mockMvc.perform(put("/api/admin/orders/{id}/confirm-payment", codOrderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/courses/{courseId}/video", pendingCourse.getId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"test-course-" + UUID.randomUUID() + "\","
                                + "\"title\":\"Test Course\",\"description\":\"Test description\","
                                + "\"price\":10.00,\"youtubeUrl\":\"https://youtu.be/GoXwIVyNvX0\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.youtubeUrl").value("https://youtu.be/GoXwIVyNvX0"));

        long deadline = System.currentTimeMillis() + 3000;
        while (notificationRepository.findByUserIdOrderByCreatedAtDesc(customerId).isEmpty()
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(25);
        }
        assertThat(notificationRepository.findByUserIdOrderByCreatedAtDesc(customerId))
                .anySatisfy(notification -> assertThat(notification.getOrderId()).isEqualTo(paidOrderId));

        StudyUser anotherCustomer = userRepository.save(new StudyUser(
                "another-" + UUID.randomUUID() + "@example.test", passwordEncoder.encode("OtherPass123!")));
        String anotherCustomerToken = loginAndGetToken(anotherCustomer.getEmail(), "OtherPass123!");
        mockMvc.perform(get("/api/orders/{id}", paidOrderId)
                        .header("Authorization", "Bearer " + anotherCustomerToken))
                .andExpect(status().isNotFound());

        assertThat(orderRepository.findByIdAndUserId(paidOrderId, customerId)).isPresent();
    }

    private String registerAndGetToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("accessToken").asText();
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("accessToken").asText();
    }
}
