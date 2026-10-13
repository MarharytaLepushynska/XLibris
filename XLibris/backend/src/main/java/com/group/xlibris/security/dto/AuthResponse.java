package com.group.xlibris.security.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static AuthResponse of(String token, long expiresIn) {
        return new AuthResponse(token, "Bearer", expiresIn);
    }
}
