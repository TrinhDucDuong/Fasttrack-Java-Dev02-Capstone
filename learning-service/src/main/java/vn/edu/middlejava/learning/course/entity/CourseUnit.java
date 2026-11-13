package vn.edu.middlejava.learning.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Cacheable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "course_units", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "unit_number"}))
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class CourseUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "unit_number", nullable = false)
    private int unitNumber;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String outline;

    protected CourseUnit() {
    }

    public CourseUnit(Course course, int unitNumber, String title, String outline) {
        this.course = course;
        this.unitNumber = unitNumber;
        this.title = title;
        this.outline = outline;
    }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public int getUnitNumber() { return unitNumber; }
    public String getTitle() { return title; }
    public String getOutline() { return outline; }
}
