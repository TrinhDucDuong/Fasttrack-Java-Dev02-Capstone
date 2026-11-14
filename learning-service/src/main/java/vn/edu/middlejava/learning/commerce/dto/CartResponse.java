package vn.edu.middlejava.learning.commerce.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long id, List<Item> items, BigDecimal total) {
    public record Item(Long courseId, String title, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }
}
