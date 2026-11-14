package vn.edu.middlejava.learning.commerce.dto;
import jakarta.validation.constraints.NotBlank;
public record CheckoutRequest(@NotBlank String paymentMethod) { }
