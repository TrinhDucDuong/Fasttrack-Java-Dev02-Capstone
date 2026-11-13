package vn.edu.middlejava.learning.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@Email @NotBlank @Size(max = 254) String email,
                              @NotBlank @Size(min = 10, max = 72) String password) {
}
