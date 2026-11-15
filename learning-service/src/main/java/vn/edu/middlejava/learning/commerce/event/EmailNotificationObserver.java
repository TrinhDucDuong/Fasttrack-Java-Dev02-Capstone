package vn.edu.middlejava.learning.commerce.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Simulated e-mail channel: writes the e-mail to the log instead of calling an SMTP server. */
@Component
public class EmailNotificationObserver implements OrderStatusObserver {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationObserver.class);

    @Override
    public String channel() {
        return "EMAIL";
    }

    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("[EMAIL] thread={} to user={} subject=\"Order #{} {}\" body=\"{}\"",
                Thread.currentThread().getName(), event.userId(), event.orderId(), event.type(), event.describe());
    }
}
