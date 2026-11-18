package com.examsphere.auth.DTOs;

public record AuthResponse(
        Long userId,
        String fullName,
        String email,
        String role,
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {}