package vn.edu.middlejava.learning.commerce.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEventPublisherTest {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @AfterEach
    void shutdown() {
        executor.shutdownNow();
    }

    @Test
    void notifiesEveryObserverOnWorkerThreadsEvenWhenOneFails() throws Exception {
        String callerThread = Thread.currentThread().getName();
        Set<String> delivered = ConcurrentHashMap.newKeySet();
        Set<String> threads = ConcurrentHashMap.newKeySet();
        OrderStatusObserver failing = observer("SMS", event -> {
            throw new IllegalStateException("SMS gateway down");
        });
        OrderStatusObserver inApp = observer("IN_APP", event -> {
            delivered.add("IN_APP");
            threads.add(Thread.currentThread().getName());
        });
        OrderStatusObserver email = observer("EMAIL", event -> {
            delivered.add("EMAIL");
            threads.add(Thread.currentThread().getName());
        });
        OrderEventPublisher publisher = new OrderEventPublisher(List.of(failing, inApp, email), executor);

        publisher.notifyObservers(event()).get(2, TimeUnit.SECONDS);

        assertThat(delivered).containsExactlyInAnyOrder("IN_APP", "EMAIL");
        assertThat(threads).doesNotContain(callerThread);
    }

    @Test
    void eventDescribesOrderForTheCustomer() {
        assertThat(event().describe()).contains("Order #12", "PENDING", "COD");
    }

    private OrderStatusChangedEvent event() {
        return new OrderStatusChangedEvent(OrderStatusChangedEvent.Type.CREATED, 12L, 7L,
                "AWAITING_PAYMENT", "COD", "PENDING", new BigDecimal("29.99"));
    }

    private OrderStatusObserver observer(String channel, java.util.function.Consumer<OrderStatusChangedEvent> action) {
        return new OrderStatusObserver() {
            @Override
            public String channel() {
                return channel;
            }

            @Override
            public void onOrderStatusChanged(OrderStatusChangedEvent event) {
                action.accept(event);
            }
        };
    }
}
