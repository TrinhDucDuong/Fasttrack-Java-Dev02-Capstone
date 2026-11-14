package vn.edu.middlejava.learning.commerce.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String method();

    String charge(BigDecimal amount);
}
