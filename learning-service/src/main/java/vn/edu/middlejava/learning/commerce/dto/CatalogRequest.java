package vn.edu.middlejava.learning.commerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CatalogRequest(
        @NotBlank @Pattern(regexp = "[a-z0-9-]{3,100}") String slug,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @NotBlank @Pattern(regexp = "https://(www\\.)?(youtube\\.com/watch\\?v=[A-Za-z0-9_-]{6,}|youtu\\.be/[A-Za-z0-9_-]{6,})") String youtubeUrl) {
}
