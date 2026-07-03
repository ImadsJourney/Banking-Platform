package com.banking.banking_platform.auth.dto;

import com.banking.banking_platform.auth.Role;

public record AuthResponse(
    String token,
    String email,
    Role role) {
}
