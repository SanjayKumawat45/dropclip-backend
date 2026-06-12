package com.dropclip.dropclip.controller;

import com.dropclip.dropclip.dto.StreakResponseDTO;
import com.dropclip.dropclip.dto.UserResponseDTO;
import com.dropclip.dropclip.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @GetMapping("/me/streak")
    public ResponseEntity<StreakResponseDTO> getCurrentUserStreak() {
        return ResponseEntity.ok(userService.getCurrentUserStreak());
    }
}
