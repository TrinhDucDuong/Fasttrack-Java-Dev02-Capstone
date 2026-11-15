package vn.edu.middlejava.learning.commerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;
import vn.edu.middlejava.learning.commerce.event.OrderStatusChangedEvent;
import vn.edu.middlejava.learning.commerce.mapper.CommerceMapper;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.service.impl.AdminOrderServiceImpl;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminOrderServiceImplTest {
    private final CustomerOrderRepository orders = mock(CustomerOrderRepository.class);
    private final ICourseEnrollmentService enrollments = mock(ICourseEnrollmentService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final AdminOrderServiceImpl service =
            new AdminOrderServiceImpl(orders, enrollments, events, new CommerceMapper());

    @Test
    void confirmingCodPaymentUnlocksCoursesAndNotifiesCustomer() {
        CustomerOrder order = pendingOrder();
        when(orders.findById(5L)).thenReturn(Optional.of(order));

        var response = service.confirmPayment(5L);

        assertThat(response.paymentStatus()).isEqualTo("PAID");
        assertThat(response.status()).isEqualTo("CONFIRMED");
        verify(enrollments).enrollPaidCourses(7L, List.of(31L));
        verify(events).publishEvent(argThat((Object event) -> event instanceof OrderStatusChangedEvent changed
                && changed.type() == OrderStatusChangedEvent.Type.PAYMENT_CONFIRMED));
    }

    @Test
    void confirmingAnAlreadyPaidOrderIsRejected() {
        CustomerOrder order = new CustomerOrder(7L, new BigDecimal("29.99"), "VNPAY", "PAID", "VNP1");
        when(orders.findById(5L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.confirmPayment(5L)).isInstanceOf(IllegalStateException.class);
        verify(enrollments, never()).enrollPaidCourses(any(), any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    void confirmingMissingOrderReturnsNotFound() {
        when(orders.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmPayment(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    private CustomerOrder pendingOrder() {
        CustomerOrder order = new CustomerOrder(7L, new BigDecimal("29.99"), "COD", "PENDING", null);
        order.addItem(31L, "Middle Java", new BigDecimal("29.99"), 1);
        return order;
    }
}
