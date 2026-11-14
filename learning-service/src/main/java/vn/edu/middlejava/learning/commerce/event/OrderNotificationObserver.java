package vn.edu.middlejava.learning.commerce.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.edu.middlejava.learning.notification.entity.UserNotification;
import vn.edu.middlejava.learning.notification.repository.UserNotificationRepository;

@Component
public class OrderNotificationObserver {
    private static final Logger log = LoggerFactory.getLogger(OrderNotificationObserver.class);

    private final UserNotificationRepository notifications;

    public OrderNotificationObserver(UserNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Async
    @TransactionalEventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        String message = "Your order " + event.orderId() + " has been created successfully.";
        notifications.save(new UserNotification(event.userId(), event.orderId(), message));
        log.info("Stored order confirmation notification for user {} and order {}",
                event.userId(), event.orderId());
    }
}
