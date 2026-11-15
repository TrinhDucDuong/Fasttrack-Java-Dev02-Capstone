package vn.edu.middlejava.learning.commerce.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.event.OrderStatusChangedEvent;
import vn.edu.middlejava.learning.commerce.mapper.CommerceMapper;
import vn.edu.middlejava.learning.commerce.payment.PaymentRequest;
import vn.edu.middlejava.learning.commerce.payment.PaymentResult;
import vn.edu.middlejava.learning.commerce.payment.PaymentStrategy;
import vn.edu.middlejava.learning.commerce.payment.PaymentStrategyFactory;
import vn.edu.middlejava.learning.commerce.pricing.CartPricingCalculator;
import vn.edu.middlejava.learning.commerce.pricing.PricedCart;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.repository.ShoppingCartRepository;
import vn.edu.middlejava.learning.commerce.service.IOrderService;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;

/** SRP: turns a cart into an order and reads a customer's orders. */
@Service
public class OrderServiceImpl implements IOrderService {
    private final ShoppingCartRepository carts;
    private final CustomerOrderRepository orders;
    private final CartPricingCalculator pricing;
    private final PaymentStrategyFactory payments;
    private final ICourseEnrollmentService enrollments;
    private final ApplicationEventPublisher events;
    private final CommerceMapper mapper;

    public OrderServiceImpl(ShoppingCartRepository carts, CustomerOrderRepository orders,
                            CartPricingCalculator pricing, PaymentStrategyFactory payments,
                            ICourseEnrollmentService enrollments, ApplicationEventPublisher events,
                            CommerceMapper mapper) {
        this.carts = carts;
        this.orders = orders;
        this.pricing = pricing;
        this.payments = payments;
        this.enrollments = enrollments;
        this.events = events;
        this.mapper = mapper;
    }

    /**
     * One transaction: save order + items, grant course access, empty the cart.
     * If any step throws, everything rolls back - no half-created order, the cart stays intact.
     */
    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        ShoppingCart cart = carts.findByUserId(userId)
                .filter(existing -> !existing.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));
        PaymentStrategy payment = payments.create(request.paymentMethod());
        PricedCart pricedCart = pricing.price(cart);

        PaymentResult paymentResult = payment.pay(new PaymentRequest(userId, pricedCart.total()));
        CustomerOrder order = orders.save(buildOrder(userId, pricedCart, payment.method(), paymentResult));

        if (order.isPaid()) {
            enrollments.enrollPaidCourses(userId, order.courseIds());
        }
        cart.clear();
        events.publishEvent(OrderStatusChangedEvent.created(order));
        return mapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        return orders.findByIdAndUserId(orderId, userId)
                .map(mapper::toOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private CustomerOrder buildOrder(Long userId, PricedCart pricedCart, String method, PaymentResult result) {
        CustomerOrder order = new CustomerOrder(userId, pricedCart.total(), method,
                result.status().name(), result.reference());
        pricedCart.lines().forEach(line ->
                order.addItem(line.courseId(), line.title(), line.unitPrice(), line.quantity()));
        return order;
    }
}
