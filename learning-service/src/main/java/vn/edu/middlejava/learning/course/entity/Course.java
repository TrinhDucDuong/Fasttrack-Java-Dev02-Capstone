package vn.edu.middlejava.learning.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Cacheable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "courses")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "price", nullable = false)
    private java.math.BigDecimal price = java.math.BigDecimal.ZERO;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    protected Course() {
    }

    public Course(String slug, String title, String description) {
        this.slug = slug;
        this.title = title;
        this.description = description;
    }

    public Course(String slug, String title, String description, java.math.BigDecimal price, String youtubeUrl) {
        this(slug, title, description);
        this.price = price;
        this.youtubeUrl = youtubeUrl;
    }

    public void updateCatalog(String title, String description, java.math.BigDecimal price, String youtubeUrl) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.youtubeUrl = youtubeUrl;
    }

    public java.math.BigDecimal getPrice() { return price; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
