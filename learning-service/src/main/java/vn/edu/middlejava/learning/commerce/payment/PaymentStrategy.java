package vn.edu.middlejava.learning.commerce.payment;

/**
 * Strategy Pattern - and the Product of the Factory Method.
 * Every payment method keeps its own algorithm behind this contract,
 * so checkout never branches on the payment type.
 */
public interface PaymentStrategy {
    PaymentResult pay(PaymentRequest request);
}
