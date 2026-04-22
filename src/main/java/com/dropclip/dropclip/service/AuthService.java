package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.*;
import com.dropclip.dropclip.entity.User;
import com.dropclip.dropclip.repository.UserRepository;
import com.dropclip.dropclip.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ── Register ──────────────────────────────────────────

    public AuthResponseDTO register(RegisterRequestDTO request) {

        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // 2. Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // 3. Build and save the new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .streakCount(0)
                .totalClips(0)
                .isActive(true)
                .build();

        userRepository.save(user);

        // 4. Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // 5. Return response
        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                mapToUserResponse(user)
        );
    }

    // ── Login ─────────────────────────────────────────────

    public AuthResponseDTO login(LoginRequestDTO request) {

        // 1. Authenticate — throws exception if wrong credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // 4. Return response
        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                mapToUserResponse(user)
        );
    }

    // ── Refresh Token ─────────────────────────────────────

    public AuthResponseDTO refresh(String refreshToken) {

        // 1. Extract email from refresh token
        String email = jwtUtil.extractEmail(refreshToken);

        // 2. Load user from DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Validate refresh token
        if (!jwtUtil.isTokenValid(refreshToken, user.getEmail())) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 4. Issue new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());

        return new AuthResponseDTO(
                newAccessToken,
                refreshToken,
                mapToUserResponse(user)
        );
    }

    // ── Helper ────────────────────────────────────────────

    private UserResponseDTO mapToUserResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .streakCount(user.getStreakCount())
                .totalClips(user.getTotalClips())
                .build();
    }
}