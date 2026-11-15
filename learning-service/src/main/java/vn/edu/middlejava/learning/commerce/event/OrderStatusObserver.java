package vn.edu.middlejava.learning.commerce.event;

/**
 * Observer Pattern: anything that wants to react to an order status change implements
 * this interface. New channels (SMS, Zalo...) are new classes; the order code is untouched.
 */
public interface OrderStatusObserver {
    String channel();

    void onOrderStatusChanged(OrderStatusChangedEvent event);
}
