package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

/** ConcreteCreator: creates the bank-transfer strategy. */
@Component
public class BankTransferPaymentProcessor extends PaymentProcessor {
    @Override
    public String method() {
        return "BANK_TRANSFER";
    }

    @Override
    protected PaymentStrategy createStrategy() {
        return new BankTransferPaymentStrategy();
    }
}
