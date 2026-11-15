package vn.edu.middlejava.learning.commerce.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * Immutable snapshot of a cart priced against the current catalog.
 * The cart view and checkout both use it, so they always agree on the numbers.
 */
public record PricedCart(List<Line> lines, BigDecimal total) {
    public PricedCart {
        lines = List.copyOf(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public List<Long> courseIds() {
        return lines.stream().map(Line::courseId).toList();
    }

    public record Line(Long courseId, String title, BigDecimal unitPrice, int quantity) {
        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
