package com.streamflix.dto;

public record AuthResponse(
        String token,
        String tokenType,
        long   expiresInMs,
        UserResponse user
) {
    public static AuthResponse of(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
