package vn.edu.middlejava.learning.commerce.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BankTransferPaymentStrategy implements PaymentStrategy {
    @Override
    public String method() {
        return "BANK_TRANSFER";
    }

    @Override
    public String charge(BigDecimal amount) {
        return "PENDING";
    }
}
