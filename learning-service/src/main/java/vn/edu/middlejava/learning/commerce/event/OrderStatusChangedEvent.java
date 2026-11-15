package vn.edu.middlejava.learning.commerce.event;

import vn.edu.middlejava.learning.commerce.entity.CustomerOrder;

import java.math.BigDecimal;

/** Message sent to every observer when an order is created or its status changes. */
public record OrderStatusChangedEvent(Type type, Long orderId, Long userId, String status,
                                      String paymentMethod, String paymentStatus, BigDecimal total) {
    public enum Type {
        CREATED,
        PAYMENT_CONFIRMED
    }

    public static OrderStatusChangedEvent created(CustomerOrder order) {
        return of(Type.CREATED, order);
    }

    public static OrderStatusChangedEvent paymentConfirmed(CustomerOrder order) {
        return of(Type.PAYMENT_CONFIRMED, order);
    }

    private static OrderStatusChangedEvent of(Type type, CustomerOrder order) {
        return new OrderStatusChangedEvent(type, order.getId(), order.getUserId(), order.getStatus(),
                order.getPaymentMethod(), order.getPaymentStatus(), order.getTotal());
    }

    public String describe() {
        return switch (type) {
            case CREATED -> "Order #%d has been created. Status: %s, payment %s via %s, total %s."
                    .formatted(orderId, status, paymentStatus, paymentMethod, total);
            case PAYMENT_CONFIRMED -> "Payment for order #%d has been confirmed. Status: %s. Your courses are unlocked."
                    .formatted(orderId, status);
        };
    }
}
