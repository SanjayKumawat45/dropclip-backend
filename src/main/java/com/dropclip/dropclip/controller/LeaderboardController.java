package com.dropclip.dropclip.controller;

import com.dropclip.dropclip.dto.ClipResponseDTO;
import com.dropclip.dropclip.service.ClipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final ClipService clipService;

    @GetMapping
    public ResponseEntity<List<ClipResponseDTO>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {

        if (limit < 1) {
            limit = 10;
        }

        if (limit > 50) {
            limit = 50;
        }

        return ResponseEntity.ok(
                clipService.getLeaderboardForActiveDrop(limit)
        );
    }
}