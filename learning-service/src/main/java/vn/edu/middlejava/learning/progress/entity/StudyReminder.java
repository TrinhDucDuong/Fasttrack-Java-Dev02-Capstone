package vn.edu.middlejava.learning.progress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "study_reminders")
public class StudyReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StudyReminder() {
    }

    public StudyReminder(Long userId, Long unitId, Instant dueAt) {
        this.userId = userId;
        this.unitId = unitId;
        this.dueAt = dueAt;
        this.createdAt = Instant.now();
    }

    public void markNotified(Instant notifiedAt) { this.notifiedAt = notifiedAt; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getUnitId() { return unitId; }
    public Instant getDueAt() { return dueAt; }
    public Instant getNotifiedAt() { return notifiedAt; }
}
