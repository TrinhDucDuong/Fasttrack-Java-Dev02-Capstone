package vn.edu.middlejava.learning.commerce.payment;

/** Cash on delivery: money is collected later, so the order waits for an admin confirmation. */
public class CodPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentResult pay(PaymentRequest request) {
        return PaymentResult.pending(null);
    }
}
