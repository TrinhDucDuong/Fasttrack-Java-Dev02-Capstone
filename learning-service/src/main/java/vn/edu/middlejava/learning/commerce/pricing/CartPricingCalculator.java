package vn.edu.middlejava.learning.commerce.pricing;

import org.springframework.stereotype.Component;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCart;
import vn.edu.middlejava.learning.commerce.entity.ShoppingCartItem;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extract Class refactoring: the pricing logic used to be duplicated inside the
 * cart and checkout code. It now has a single owner and a single reason to change.
 */
@Component
public class CartPricingCalculator {
    private final ICourseRepository courses;

    public CartPricingCalculator(ICourseRepository courses) {
        this.courses = courses;
    }

    public PricedCart price(ShoppingCart cart) {
        Map<Long, Course> catalog = loadCatalog(cart.getItems());
        List<PricedCart.Line> lines = cart.getItems().stream()
                .map(item -> toLine(item, catalog))
                .toList();
        return new PricedCart(lines, sumLineTotals(lines));
    }

    private Map<Long, Course> loadCatalog(List<ShoppingCartItem> items) {
        List<Long> courseIds = items.stream().map(ShoppingCartItem::getCourseId).toList();
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Course> catalog = courses.findAllById(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));
        if (catalog.size() != courseIds.size()) {
            throw new ResourceNotFoundException("A course in the cart is unavailable");
        }
        return catalog;
    }

    private PricedCart.Line toLine(ShoppingCartItem item, Map<Long, Course> catalog) {
        Course course = catalog.get(item.getCourseId());
        return new PricedCart.Line(course.getId(), course.getTitle(), course.getPrice(), item.getQuantity());
    }

    private BigDecimal sumLineTotals(List<PricedCart.Line> lines) {
        return lines.stream()
                .map(PricedCart.Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
