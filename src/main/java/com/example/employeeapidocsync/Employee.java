package com.example.employeeapidocsync;

import io.swagger.v3.oas.annotations.media.Schema;

public record Employee(
        Long id,
        String name,
        String email,
        @Schema(description = "Optional department, stored as given", nullable = true) String department) {
}
