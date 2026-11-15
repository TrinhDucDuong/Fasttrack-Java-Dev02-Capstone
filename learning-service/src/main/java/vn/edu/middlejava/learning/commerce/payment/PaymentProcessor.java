package vn.edu.middlejava.learning.commerce.payment;

/**
 * Factory Method Pattern (GoF) - the Creator.
 *
 * <p>{@link #process(PaymentRequest)} holds the payment workflow shared by every method,
 * but it does not know which {@link PaymentStrategy} (the Product) it works with.
 * Each subclass (ConcreteCreator) overrides the factory method {@link #createStrategy()}
 * to decide that. Adding a payment method = adding one Creator + one Strategy;
 * no existing class changes (Open/Closed Principle).
 */
public abstract class PaymentProcessor {

    /** Code the client sends in the checkout request, for example COD or VNPAY. */
    public abstract String method();

    /** The factory method: subclasses decide which strategy object is created. */
    protected abstract PaymentStrategy createStrategy();

    /** Template operation used by checkout; relies on the product created by the subclass. */
    public final PaymentResult process(PaymentRequest request) {
        if (request.amount() == null || request.amount().signum() < 0) {
            throw new IllegalArgumentException("Payment amount must not be negative");
        }
        PaymentStrategy strategy = createStrategy();
        return strategy.pay(request);
    }
}
