package vn.edu.middlejava.learning.commerce.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;
import vn.edu.middlejava.learning.commerce.event.OrderStatusChangedEvent;
import vn.edu.middlejava.learning.commerce.mapper.CommerceMapper;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.service.IAdminOrderService;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;

/** SRP: back-office order operations for ADMIN only. */
@Service
public class AdminOrderServiceImpl implements IAdminOrderService {
    private final CustomerOrderRepository orders;
    private final ICourseEnrollmentService enrollments;
    private final ApplicationEventPublisher events;
    private final CommerceMapper mapper;

    public AdminOrderServiceImpl(CustomerOrderRepository orders, ICourseEnrollmentService enrollments,
                                 ApplicationEventPublisher events, CommerceMapper mapper) {
        this.orders = orders;
        this.enrollments = enrollments;
        this.events = events;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orders.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse confirmPayment(Long orderId) {
        CustomerOrder order = orders.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.confirmPayment();
        enrollments.enrollPaidCourses(order.getUserId(), order.courseIds());
        events.publishEvent(OrderStatusChangedEvent.paymentConfirmed(order));
        return mapper.toOrderResponse(order);
    }
}
