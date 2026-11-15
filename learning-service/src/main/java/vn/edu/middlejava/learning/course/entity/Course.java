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

    @Column(nullable = false)
    private boolean active = true;

    /** Object key in S3 / SeaweedFS. The video bytes live in object storage, never in the database. */
    @Column(name = "video_key", length = 300)
    private String videoKey;

    @Column(name = "video_content_type", length = 100)
    private String videoContentType;

    @Column(name = "video_size_bytes")
    private Long videoSizeBytes;

    @Column(name = "video_uploaded_at")
    private java.time.Instant videoUploadedAt;

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

    /** Soft delete: past orders keep pointing at the course, but it can no longer be sold. */
    public void deactivate() {
        this.active = false;
    }

    public void attachVideo(String key, String contentType, long sizeBytes) {
        this.videoKey = key;
        this.videoContentType = contentType;
        this.videoSizeBytes = sizeBytes;
        this.videoUploadedAt = java.time.Instant.now();
    }

    public boolean hasStoredVideo() {
        return videoKey != null && !videoKey.isBlank();
    }

    public String getVideoKey() { return videoKey; }
    public String getVideoContentType() { return videoContentType; }
    public Long getVideoSizeBytes() { return videoSizeBytes; }
    public java.time.Instant getVideoUploadedAt() { return videoUploadedAt; }
    public boolean isActive() { return active; }
    public java.math.BigDecimal getPrice() { return price; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
