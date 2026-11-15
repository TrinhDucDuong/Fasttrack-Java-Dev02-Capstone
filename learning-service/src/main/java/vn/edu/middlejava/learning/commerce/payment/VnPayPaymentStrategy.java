package vn.edu.middlejava.learning.commerce.payment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/** Simulated VNPay gateway: approves the payment and returns a gateway transaction number. */
public class VnPayPaymentStrategy implements PaymentStrategy {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public PaymentResult pay(PaymentRequest request) {
        if (request.amount().signum() <= 0) {
            throw new IllegalArgumentException("VNPay amount must be positive");
        }
        String transactionNo = "VNP" + TIMESTAMP.format(ZonedDateTime.now(VIETNAM))
                + ThreadLocalRandom.current().nextInt(1000, 10000);
        return PaymentResult.paid(transactionNo);
    }
}
