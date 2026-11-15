package vn.edu.middlejava.learning.commerce.payment;

import java.math.BigDecimal;

public record PaymentRequest(Long userId, BigDecimal amount) {
}
