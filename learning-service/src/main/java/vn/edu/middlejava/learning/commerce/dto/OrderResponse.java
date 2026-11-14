package vn.edu.middlejava.learning.commerce.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, BigDecimal total, String paymentMethod, String paymentStatus,
                            String status, Instant createdAt, List<Item> items) {
    public record Item(Long courseId, String courseTitle, BigDecimal price, int quantity) {
    }
}
