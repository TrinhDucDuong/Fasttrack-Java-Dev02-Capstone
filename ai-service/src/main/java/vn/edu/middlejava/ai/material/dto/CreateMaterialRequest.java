package vn.edu.middlejava.ai.material.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMaterialRequest(
        @NotNull Long courseId,
        @NotNull Long unitId,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 100000) String content) {
}
