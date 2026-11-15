package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

/** Cash on delivery: money is collected later, so the order waits for an admin confirmation. */
@Component
public class CodPaymentStrategy implements PaymentStrategy {
    @Override
    public String method() {
        return "COD";
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        return PaymentResult.pending(null);
    }
}
