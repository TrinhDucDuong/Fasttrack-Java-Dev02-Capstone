package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

/** ConcreteCreator: creates the simulated VNPay strategy. */
@Component
public class VnPayPaymentProcessor extends PaymentProcessor {
    @Override
    public String method() {
        return "VNPAY";
    }

    @Override
    protected PaymentStrategy createStrategy() {
        return new VnPayPaymentStrategy();
    }
}
