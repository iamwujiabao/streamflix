package com.streamflix.service;

import com.streamflix.dto.AuthResponse;
import com.streamflix.dto.LoginRequest;
import com.streamflix.dto.RegisterRequest;
import com.streamflix.dto.UserResponse;
import com.streamflix.entity.User;
import com.streamflix.exception.BadRequestException;
import com.streamflix.repository.UserRepository;
import com.streamflix.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      tokenProvider;

    /** Register a new account and immediately issue a token (auto-login). */
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username()))
            throw new BadRequestException("Username already taken");
        if (userRepository.existsByEmail(req.email()))
            throw new BadRequestException("Email already registered");

        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .country(req.country())
                .role(User.Role.USER)
                .isActive(true)
                .build();
        userRepository.save(user);

        return issueToken(user);
    }

    /** Authenticate username + password and return a token. */
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        return issueToken(user);
    }

    private AuthResponse issueToken(User user) {
        String token = tokenProvider.generateToken(user.getUsername(), user.getRole().name());
        return AuthResponse.of(token, tokenProvider.getExpirationMs(), UserResponse.fromEntity(user));
    }
}
