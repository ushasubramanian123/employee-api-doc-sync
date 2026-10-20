package com.example.employeeapidocsync;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateEmployeeRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @Schema(description = "Optional department, stored as given", nullable = true) String department) {
}
