package vn.edu.middlejava.learning.commerce.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.edu.middlejava.learning.notification.entity.UserNotification;
import vn.edu.middlejava.learning.notification.repository.UserNotificationRepository;

/** Stores the message so the customer can read it from GET /api/notifications. */
@Component
public class InAppNotificationObserver implements OrderStatusObserver {
    private static final Logger log = LoggerFactory.getLogger(InAppNotificationObserver.class);

    private final UserNotificationRepository notifications;

    public InAppNotificationObserver(UserNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public String channel() {
        return "IN_APP";
    }

    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        notifications.save(new UserNotification(event.userId(), event.orderId(), event.describe()));
        log.info("[IN_APP] thread={} user={} order={} type={}",
                Thread.currentThread().getName(), event.userId(), event.orderId(), event.type());
    }
}
