package vn.edu.middlejava.ai.material.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "materials")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Material() {
    }

    public Material(Long courseId, Long unitId, String title, String content) {
        this.courseId = courseId;
        this.unitId = unitId;
        this.title = title;
        this.content = content;
        this.status = MaterialStatus.RECEIVED;
        this.createdAt = Instant.now();
    }

    public void setStatus(MaterialStatus status) { this.status = status; }
    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Long getUnitId() { return unitId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public MaterialStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
