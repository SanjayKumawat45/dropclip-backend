package com.dropclip.dropclip.controller;

import com.dropclip.dropclip.dto.StreakResponseDTO;
import com.dropclip.dropclip.dto.UserResponseDTO;
import com.dropclip.dropclip.dto.UserSearchResponseDTO;
import com.dropclip.dropclip.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =====================================================
    // CURRENT USER
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {

        return ResponseEntity.ok(
                userService.getCurrentUserProfile()
        );
    }

    // =====================================================
    // CURRENT USER STREAK
    // =====================================================

    @GetMapping("/me/streak")
    public ResponseEntity<StreakResponseDTO> getCurrentUserStreak() {

        return ResponseEntity.ok(
                userService.getCurrentUserStreak()
        );
    }

    // =====================================================
    // SEARCH USERS
    // =====================================================

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponseDTO>> searchUsers(
            @RequestParam String query
    ) {

        return ResponseEntity.ok(
                userService.searchUsers(query)
        );
    }

    // =====================================================
    // PUBLIC USER PROFILE
    // =====================================================

    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDTO> getUserProfile(
            @PathVariable String username
    ) {

        return ResponseEntity.ok(
                userService.getUserProfileByUsername(username)
        );
    }
}