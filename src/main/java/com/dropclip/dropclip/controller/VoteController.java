package com.dropclip.dropclip.controller;

import com.dropclip.dropclip.dto.ClipResponseDTO;
import com.dropclip.dropclip.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/clips/{clipId}/vote")
    public ResponseEntity<Void> vote(@PathVariable UUID clipId) {
        voteService.vote(clipId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/drops/{dropId}/leaderboard")
    public ResponseEntity<List<ClipResponseDTO>> getLeaderboard(
            @PathVariable UUID dropId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(
                voteService.getLeaderboard(dropId, limit));
    }
}