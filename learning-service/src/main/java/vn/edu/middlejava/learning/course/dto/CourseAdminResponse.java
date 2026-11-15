package vn.edu.middlejava.learning.course.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CourseAdminResponse(Long id, String slug, String title, String description,
                                  BigDecimal price, String youtubeUrl, boolean active,
                                  String videoKey, Long videoSizeBytes, Instant videoUploadedAt) {
}
