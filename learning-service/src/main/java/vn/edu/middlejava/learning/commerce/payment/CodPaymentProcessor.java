package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

/** ConcreteCreator: creates the cash-on-delivery strategy. */
@Component
public class CodPaymentProcessor extends PaymentProcessor {
    @Override
    public String method() {
        return "COD";
    }

    @Override
    protected PaymentStrategy createStrategy() {
        return new CodPaymentStrategy();
    }
}
