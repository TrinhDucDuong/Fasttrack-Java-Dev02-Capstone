package vn.edu.middlejava.learning.commerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.event.OrderStatusChangedEvent;
import vn.edu.middlejava.learning.commerce.mapper.CommerceMapper;
import vn.edu.middlejava.learning.commerce.payment.BankTransferPaymentProcessor;
import vn.edu.middlejava.learning.commerce.payment.CodPaymentProcessor;
import vn.edu.middlejava.learning.commerce.payment.PaymentProcessorRegistry;
import vn.edu.middlejava.learning.commerce.payment.VnPayPaymentProcessor;
import vn.edu.middlejava.learning.commerce.pricing.CartPricingCalculator;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.repository.ShoppingCartRepository;
import vn.edu.middlejava.learning.commerce.service.impl.OrderServiceImpl;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static vn.edu.middlejava.learning.commerce.CommerceTestData.course;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderServiceImpl - checkout turns the cart into an order")
class OrderServiceImplTest {
    private static final Long USER_ID = 7L;

    @Mock
    private ShoppingCartRepository carts;
    @Mock
    private CustomerOrderRepository orders;
    @Mock
    private ICourseRepository courses;
    @Mock
    private ICourseEnrollmentService enrollments;
    @Mock
    private ApplicationEventPublisher events;

    private OrderServiceImpl service;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        PaymentProcessorRegistry payments = new PaymentProcessorRegistry(List.of(
                new CodPaymentProcessor(), new VnPayPaymentProcessor(), new BankTransferPaymentProcessor()));
        service = new OrderServiceImpl(carts, orders, new CartPricingCalculator(courses), payments,
                enrollments, events, new CommerceMapper());

        cart = new ShoppingCart(USER_ID);
        cart.add(31L, 2);
        Course course = course(31L, "Middle Java", "29.99");
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(courses.findAllById(anyList())).thenReturn(List.of(course));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void vnpayCheckoutIsPaidUnlocksCoursesClearsCartAndNotifies() {
        OrderResponse order = service.checkout(USER_ID, new CheckoutRequest("VNPAY"));

        assertThat(order.total()).isEqualByComparingTo("59.98");
        assertThat(order.paymentMethod()).isEqualTo("VNPAY");
        assertThat(order.paymentStatus()).isEqualTo("PAID");
        assertThat(order.paymentReference()).startsWith("VNP");
        assertThat(order.status()).isEqualTo("CONFIRMED");
        assertThat(order.items()).singleElement().satisfies(item -> {
            assertThat(item.courseTitle()).isEqualTo("Middle Java");
            assertThat(item.price()).isEqualByComparingTo("29.99");
            assertThat(item.quantity()).isEqualTo(2);
        });
        assertThat(cart.getItems()).isEmpty();
        verify(enrollments).enrollPaidCourses(USER_ID, List.of(31L));

        ArgumentCaptor<OrderStatusChangedEvent> event = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().type()).isEqualTo(OrderStatusChangedEvent.Type.CREATED);
        assertThat(event.getValue().userId()).isEqualTo(USER_ID);
    }

    @Test
    void codCheckoutWaitsForPaymentAndDoesNotUnlockCourses() {
        OrderResponse order = service.checkout(USER_ID, new CheckoutRequest("cod"));

        assertThat(order.paymentMethod()).isEqualTo("COD");
        assertThat(order.paymentStatus()).isEqualTo("PENDING");
        assertThat(order.status()).isEqualTo("AWAITING_PAYMENT");
        verify(enrollments, never()).enrollPaidCourses(any(), any());
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void checkoutRejectsAnEmptyCart() {
        when(carts.findByUserId(USER_ID)).thenReturn(Optional.of(new ShoppingCart(USER_ID)));

        assertThatThrownBy(() -> service.checkout(USER_ID, new CheckoutRequest("VNPAY")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cart is empty");
        verify(orders, never()).save(any());
    }

    @Test
    void unsupportedPaymentMethodCreatesNothing() {
        assertThatThrownBy(() -> service.checkout(USER_ID, new CheckoutRequest("BITCOIN")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported payment method");
        verify(orders, never()).save(any());
        verify(events, never()).publishEvent(any());
        assertThat(cart.getItems()).hasSize(1);
    }
}
