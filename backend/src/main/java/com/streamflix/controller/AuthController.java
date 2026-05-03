package com.streamflix.controller;

import com.streamflix.dto.*;
import com.streamflix.entity.User;
import com.streamflix.service.AuthService;
import com.streamflix.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, log in, and identity")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Create a new account, returns JWT")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Registration successful", authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and get a JWT")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Login successful", authService.login(req));
    }

    @GetMapping("/me")
    @Operation(summary = "Return the current authenticated user")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserDetails principal) {
        User u = userService.findByUsername(principal.getUsername());
        return ApiResponse.ok(UserResponse.fromEntity(u));
    }

    @GetMapping("/health")
    @Operation(summary = "Liveness probe")
    public ApiResponse<Map<String,Object>> health() {
        return ApiResponse.ok(Map.of(
                "status",    "UP",
                "service",   "streamflix-backend",
                "timestamp", Instant.now().toString()));
    }
}
