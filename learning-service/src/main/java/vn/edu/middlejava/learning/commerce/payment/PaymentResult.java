package vn.edu.middlejava.learning.commerce.payment;

public record PaymentResult(PaymentStatus status, String reference) {
    public static PaymentResult paid(String reference) {
        return new PaymentResult(PaymentStatus.PAID, reference);
    }

    public static PaymentResult pending(String reference) {
        return new PaymentResult(PaymentStatus.PENDING, reference);
    }
}
