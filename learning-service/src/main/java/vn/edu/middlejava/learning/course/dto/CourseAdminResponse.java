package vn.edu.middlejava.learning.course.dto;

import java.math.BigDecimal;

public record CourseAdminResponse(Long id, String slug, String title, String description,
                                  BigDecimal price, String youtubeUrl, boolean active) {
}
