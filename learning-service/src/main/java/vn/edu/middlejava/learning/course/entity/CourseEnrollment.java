package vn.edu.middlejava.learning.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "course_enrollments", uniqueConstraints =
        @UniqueConstraint(columnNames = {"user_id", "course_id"}))
public class CourseEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected CourseEnrollment() {
    }

    public CourseEnrollment(Long userId, Long courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public Instant getCreatedAt() { return createdAt; }
}
