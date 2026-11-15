package vn.edu.middlejava.learning.course.video.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmVideoRequest(@NotBlank String key) {
}
