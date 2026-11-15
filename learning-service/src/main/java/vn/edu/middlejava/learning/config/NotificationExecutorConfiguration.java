package vn.edu.middlejava.learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class NotificationExecutorConfiguration {
    /** Small dedicated pool so notification work never competes with HTTP request threads. */
    @Bean(name = "orderNotificationExecutor", destroyMethod = "shutdown")
    public ExecutorService orderNotificationExecutor() {
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory threads = runnable -> {
            Thread thread = new Thread(runnable, "order-notify-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(2, threads);
    }
}
