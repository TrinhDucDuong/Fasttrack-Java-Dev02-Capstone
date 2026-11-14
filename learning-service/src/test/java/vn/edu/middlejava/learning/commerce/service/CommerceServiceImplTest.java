package vn.edu.middlejava.learning.commerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.event.OrderCreatedEvent;
import vn.edu.middlejava.learning.commerce.payment.BankTransferPaymentStrategy;
import vn.edu.middlejava.learning.commerce.payment.PaymentStrategyFactory;
import vn.edu.middlejava.learning.commerce.payment.SimulatedPaymentStrategy;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.repository.ShoppingCartRepository;
import vn.edu.middlejava.learning.commerce.service.impl.CommerceServiceImpl;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommerceServiceImplTest {
    private final ShoppingCartRepository carts = mock(ShoppingCartRepository.class);
    private final CustomerOrderRepository orders = mock(CustomerOrderRepository.class);
    private final ICourseRepository courses = mock(ICourseRepository.class);
    private final ICourseEnrollmentService enrollments = mock(ICourseEnrollmentService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final PaymentStrategyFactory paymentFactory = new PaymentStrategyFactory(
            List.of(new BankTransferPaymentStrategy(), new SimulatedPaymentStrategy()));
    private final CommerceServiceImpl service = new CommerceServiceImpl(carts, orders, courses,
            enrollments, paymentFactory, events);

    @Test
    void checkoutSnapshotsCurrentPriceClearsCartAndPublishesEvent() {
        ShoppingCart cart = new ShoppingCart(7L);
        cart.add(31L, 2);
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(31L);
        when(course.getTitle()).thenReturn("Middle Java");
        when(course.getPrice()).thenReturn(new BigDecimal("29.99"));
        when(carts.findByUserId(7L)).thenReturn(Optional.of(cart));
        when(courses.findAllById(List.of(31L))).thenReturn(List.of(course));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var order = service.checkout(7L, new CheckoutRequest("SIMULATED"));

        assertThat(order.total()).isEqualByComparingTo("59.98");
        assertThat(order.paymentStatus()).isEqualTo("PAID");
        assertThat(order.status()).isEqualTo("CONFIRMED");
        assertThat(order.items()).singleElement().satisfies(item -> {
            assertThat(item.courseTitle()).isEqualTo("Middle Java");
            assertThat(item.price()).isEqualByComparingTo("29.99");
            assertThat(item.quantity()).isEqualTo(2);
        });
        assertThat(cart.getItems()).isEmpty();
        verify(events).publishEvent(new OrderCreatedEvent(null, 7L));
        verify(enrollments).enrollPaidCourses(7L, List.of(31L));
    }

    @Test
    void bankTransferLeavesPaymentPending() {
        ShoppingCart cart = new ShoppingCart(7L);
        cart.add(31L, 1);
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(31L);
        when(course.getTitle()).thenReturn("Spring Boot Microservices");
        when(course.getPrice()).thenReturn(new BigDecimal("39.99"));
        when(carts.findByUserId(7L)).thenReturn(Optional.of(cart));
        when(courses.findAllById(List.of(31L))).thenReturn(List.of(course));
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var order = service.checkout(7L, new CheckoutRequest("BANK_TRANSFER"));

        assertThat(order.paymentStatus()).isEqualTo("PENDING");
        assertThat(order.status()).isEqualTo("AWAITING_PAYMENT");
        verify(enrollments, never()).enrollPaidCourses(any(), any());
    }

    @Test
    void checkoutRejectsAnEmptyCart() {
        when(carts.findByUserId(7L)).thenReturn(Optional.of(new ShoppingCart(7L)));

        assertThatThrownBy(() -> service.checkout(7L, new CheckoutRequest("SIMULATED")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cart is empty");
        verify(orders, never()).save(any());
    }
}
