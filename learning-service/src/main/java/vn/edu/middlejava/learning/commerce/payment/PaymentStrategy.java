package vn.edu.middlejava.learning.commerce.payment;

/**
 * Strategy Pattern: every payment method keeps its own algorithm behind the same
 * contract, so checkout never branches on the payment type.
 */
public interface PaymentStrategy {
    /** Code the client sends in the checkout request, for example COD or VNPAY. */
    String method();

    PaymentResult pay(PaymentRequest request);
}
