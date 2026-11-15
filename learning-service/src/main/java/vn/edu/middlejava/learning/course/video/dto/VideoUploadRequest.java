package vn.edu.middlejava.learning.course.video.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record VideoUploadRequest(@NotBlank String fileName, @NotBlank String contentType, @Positive long sizeBytes) {
}
