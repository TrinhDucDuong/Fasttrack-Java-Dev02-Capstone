package vn.edu.middlejava.learning.progress.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.progress.entity.StudyReminder;
import vn.edu.middlejava.learning.progress.event.ReminderDueEvent;
import vn.edu.middlejava.learning.progress.repository.IStudyReminderRepository;

import java.time.Instant;

@Component
public class StudyReminderScheduler {
    private final IStudyReminderRepository reminderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    public StudyReminderScheduler(IStudyReminderRepository reminderRepository, ApplicationEventPublisher eventPublisher, MeterRegistry meterRegistry) {
        this.reminderRepository = reminderRepository;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelayString = "${app.reminders.poll-interval-ms:60000}")
    @Transactional
    public void publishDueReminders() {
        Instant now = Instant.now();
        for (StudyReminder reminder : reminderRepository.findDue(now)) {
            reminder.markNotified(now);
            eventPublisher.publishEvent(new ReminderDueEvent(reminder.getUserId(), reminder.getUnitId(), reminder.getDueAt()));
            meterRegistry.counter("studycoach.reminders.published").increment();
        }
    }
}
