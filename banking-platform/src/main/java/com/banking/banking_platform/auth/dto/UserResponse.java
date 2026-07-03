package com.banking.banking_platform.auth.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.banking.banking_platform.auth.Role;

public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    Role role,
    LocalDateTime createdAt,
    boolean isActive) {
}
