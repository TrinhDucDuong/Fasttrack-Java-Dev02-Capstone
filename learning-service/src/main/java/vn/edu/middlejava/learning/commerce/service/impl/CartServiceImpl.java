package vn.edu.middlejava.learning.commerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.commerce.dto.AddCartItemRequest;
import vn.edu.middlejava.learning.commerce.dto.CartResponse;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.mapper.CommerceMapper;
import vn.edu.middlejava.learning.commerce.pricing.CartPricingCalculator;
import vn.edu.middlejava.learning.commerce.repository.ShoppingCartRepository;
import vn.edu.middlejava.learning.commerce.service.ICartService;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

/** SRP: this service only manages the shopping cart. Checkout lives in OrderServiceImpl. */
@Service
public class CartServiceImpl implements ICartService {
    private final ShoppingCartRepository carts;
    private final ICourseRepository courses;
    private final CartPricingCalculator pricing;
    private final CommerceMapper mapper;

    public CartServiceImpl(ShoppingCartRepository carts, ICourseRepository courses,
                           CartPricingCalculator pricing, CommerceMapper mapper) {
        this.carts = carts;
        this.courses = courses;
        this.pricing = pricing;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        ShoppingCart cart = carts.findByUserId(userId).orElseGet(() -> new ShoppingCart(userId));
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddCartItemRequest request) {
        Course course = findSellableCourse(request.courseId());
        ShoppingCart cart = findOrCreateCart(userId);
        cart.add(course.getId(), request.quantity());
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long courseId, int quantity) {
        ShoppingCart cart = findCart(userId);
        cart.setQuantity(courseId, quantity);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long courseId) {
        if (!findCart(userId).remove(courseId)) {
            throw new ResourceNotFoundException("Course is not in cart");
        }
    }

    // ---- Extract Method refactoring: small, named steps reused by every operation ----

    private Course findSellableCourse(Long courseId) {
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (!course.isActive()) {
            throw new IllegalArgumentException("Course is no longer for sale");
        }
        return course;
    }

    private ShoppingCart findOrCreateCart(Long userId) {
        return carts.findByUserId(userId).orElseGet(() -> carts.save(new ShoppingCart(userId)));
    }

    private ShoppingCart findCart(Long userId) {
        return carts.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartResponse toResponse(ShoppingCart cart) {
        return mapper.toCartResponse(cart.getId(), pricing.price(cart));
    }
}
