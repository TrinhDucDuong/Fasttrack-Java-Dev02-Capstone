package vn.edu.middlejava.learning.progress.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LoggingReminderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingReminderListener.class);

    @EventListener
    public void handle(ReminderDueEvent event) {
        LOGGER.info("Study reminder due for user {} and Unit {} at {}", event.userId(), event.unitId(), event.dueAt());
    }
}
