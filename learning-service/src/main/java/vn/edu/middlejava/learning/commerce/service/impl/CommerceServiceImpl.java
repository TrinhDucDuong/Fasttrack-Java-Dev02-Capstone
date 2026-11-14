package vn.edu.middlejava.learning.commerce.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.dto.CheckoutRequest;
import vn.edu.middlejava.learning.commerce.dto.OrderResponse;
import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCartItem;
import vn.edu.middlejava.learning.commerce.event.OrderCreatedEvent;
import vn.edu.middlejava.learning.commerce.payment.PaymentStrategy;
import vn.edu.middlejava.learning.commerce.payment.PaymentStrategyFactory;
import vn.edu.middlejava.learning.commerce.repository.CustomerOrderRepository;
import vn.edu.middlejava.learning.commerce.repository.ShoppingCartRepository;
import vn.edu.middlejava.learning.commerce.service.ICommerceService;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.service.ICourseEnrollmentService;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommerceServiceImpl implements ICommerceService {
    private final ShoppingCartRepository carts;
    private final CustomerOrderRepository orders;
    private final ICourseRepository courses;
    private final ICourseEnrollmentService enrollments;
    private final PaymentStrategyFactory payments;
    private final ApplicationEventPublisher events;

    public CommerceServiceImpl(ShoppingCartRepository carts,
                               CustomerOrderRepository orders,
                               ICourseRepository courses,
                               ICourseEnrollmentService enrollments,
                               PaymentStrategyFactory payments,
                               ApplicationEventPublisher events) {
        this.carts = carts;
        this.orders = orders;
        this.courses = courses;
        this.enrollments = enrollments;
        this.payments = payments;
        this.events = events;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        ShoppingCart cart = carts.findByUserId(userId).orElseGet(() -> new ShoppingCart(userId));
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddCartItemRequest request) {
        Course course = courses.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        ShoppingCart cart = carts.findByUserId(userId).orElseGet(() -> carts.save(new ShoppingCart(userId)));
        cart.add(course.getId(), request.quantity());
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long courseId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least one");
        }
        ShoppingCart cart = findCart(userId);
        cart.setQuantity(courseId, quantity);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long courseId) {
        ShoppingCart cart = findCart(userId);
        if (cart.getItems().stream().noneMatch(item -> item.getCourseId().equals(courseId))) {
            throw new ResourceNotFoundException("Course is not in cart");
        }
        cart.remove(courseId);
    }

    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        ShoppingCart cart = findCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Map<Long, Course> catalog = loadCartCourses(cart);
        BigDecimal total = cart.getItems().stream()
                .map(item -> catalog.get(item.getCourseId()).getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentStrategy payment = payments.get(request.paymentMethod());
        CustomerOrder order = new CustomerOrder(userId, total, payment.method(), payment.charge(total));
        cart.getItems().forEach(item -> {
            Course course = catalog.get(item.getCourseId());
            order.addItem(course.getId(), course.getTitle(), course.getPrice(), item.getQuantity());
        });

        CustomerOrder savedOrder = orders.save(order);
        if ("PAID".equals(savedOrder.getPaymentStatus())) {
            enrollments.enrollPaidCourses(userId, cart.getItems().stream()
                    .map(ShoppingCartItem::getCourseId)
                    .toList());
        }
        cart.clear();
        events.publishEvent(new OrderCreatedEvent(savedOrder.getId(), userId));
        return toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        return orders.findByIdAndUserId(orderId, userId)
                .map(this::toOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private ShoppingCart findCart(Long userId) {
        return carts.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private Map<Long, Course> loadCartCourses(ShoppingCart cart) {
        List<Long> courseIds = cart.getItems().stream()
                .map(ShoppingCartItem::getCourseId)
                .toList();
        Map<Long, Course> catalog = courses.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));
        if (catalog.size() != courseIds.size()) {
            throw new ResourceNotFoundException("A course in the cart is unavailable");
        }
        return catalog;
    }

    private CartResponse toCartResponse(ShoppingCart cart) {
        Map<Long, Course> catalog = courses.findAllById(cart.getItems().stream()
                        .map(ShoppingCartItem::getCourseId)
                        .toList()).stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        List<CartResponse.Item> items = cart.getItems().stream()
                .map(item -> {
                    Course course = catalog.get(item.getCourseId());
                    if (course == null) {
                        throw new ResourceNotFoundException("A course in the cart is unavailable");
                    }
                    BigDecimal lineTotal = course.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartResponse.Item(course.getId(), course.getTitle(), course.getPrice(),
                            item.getQuantity(), lineTotal);
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartResponse.Item::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total);
    }

    private OrderResponse toOrderResponse(CustomerOrder order) {
        List<OrderResponse.Item> items = order.getItems().stream()
                .map(item -> new OrderResponse.Item(item.getCourseId(), item.getCourseTitle(),
                        item.getPrice(), item.getQuantity()))
                .toList();
        return new OrderResponse(order.getId(), order.getTotal(), order.getPaymentMethod(),
                order.getPaymentStatus(), order.getStatus(), order.getCreatedAt(), items);
    }
}
