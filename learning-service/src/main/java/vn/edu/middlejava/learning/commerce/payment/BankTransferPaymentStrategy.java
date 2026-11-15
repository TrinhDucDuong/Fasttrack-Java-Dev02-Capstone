package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

import java.util.UUID;

/** Bank transfer: returns the transfer note the customer must use; an admin confirms it later. */
@Component
public class BankTransferPaymentStrategy implements PaymentStrategy {
    @Override
    public String method() {
        return "BANK_TRANSFER";
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        return PaymentResult.pending("BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
