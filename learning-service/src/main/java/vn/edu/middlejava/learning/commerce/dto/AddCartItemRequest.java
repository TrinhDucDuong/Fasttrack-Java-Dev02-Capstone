package vn.edu.middlejava.learning.commerce.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
public record AddCartItemRequest(@NotNull Long courseId, @Min(1) int quantity) { }
