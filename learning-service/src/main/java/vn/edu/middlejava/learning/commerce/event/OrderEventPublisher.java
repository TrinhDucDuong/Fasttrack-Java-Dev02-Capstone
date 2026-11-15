package vn.edu.middlejava.learning.commerce.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Observer Pattern - the subject. It keeps the list of observers and notifies each of
 * them on its own worker thread (CompletableFuture + ExecutorService), only after the
 * order transaction has committed. A slow or failing channel never blocks checkout
 * and never stops the other channels.
 */
@Component
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final List<OrderStatusObserver> observers;
    private final ExecutorService executor;

    public OrderEventPublisher(List<OrderStatusObserver> observers,
                               @Qualifier("orderNotificationExecutor") ExecutorService executor) {
        this.observers = List.copyOf(observers);
        this.executor = executor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        notifyObservers(event);
    }

    public CompletableFuture<Void> notifyObservers(OrderStatusChangedEvent event) {
        log.info("Dispatching {} for order {} to {} observers", event.type(), event.orderId(), observers.size());
        CompletableFuture<?>[] deliveries = observers.stream()
                .map(observer -> CompletableFuture
                        .runAsync(() -> observer.onOrderStatusChanged(event), executor)
                        .exceptionally(error -> {
                            log.warn("Observer {} failed for order {}: {}",
                                    observer.channel(), event.orderId(), error.getMessage());
                            return null;
                        }))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(deliveries);
    }
}
