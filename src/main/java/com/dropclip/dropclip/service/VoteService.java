package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.ClipResponseDTO;
import com.dropclip.dropclip.entity.Clip;
import com.dropclip.dropclip.entity.User;
import com.dropclip.dropclip.entity.Vote;
import com.dropclip.dropclip.exception.ApiException;
import com.dropclip.dropclip.repository.ClipRepository;
import com.dropclip.dropclip.repository.UserRepository;
import com.dropclip.dropclip.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final ClipRepository clipRepository;
    private final UserRepository userRepository;
    private final LeaderboardService leaderboardService;

    // ── Get current logged in user ────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Vote on a clip ────────────────────────────────────


    public void toggleVote(UUID clipId) {

        User user = getCurrentUser();

        Clip clip = clipRepository.findById(clipId)
                .orElseThrow(() ->
                        new ApiException(
                                "Clip not found",
                                HttpStatus.NOT_FOUND
                        ));

        Optional<Vote> existingVote =
                voteRepository.findByUserIdAndClipId(
                        user.getId(),
                        clipId
                );

        if (existingVote.isPresent()) {

            // Remove existing vote
            voteRepository.delete(existingVote.get());

            clip.setVoteCount(
                    Math.max(0, clip.getVoteCount() - 1)
            );

        } else {

            // Add new vote
            Vote vote = Vote.builder()
                    .user(user)
                    .clip(clip)
                    .build();

            voteRepository.save(vote);

            clip.setVoteCount(
                    clip.getVoteCount() + 1
            );
        }

        clipRepository.save(clip);
    }

    @Transactional
    public void vote(UUID clipId) {

        User user = getCurrentUser();

        // 1. Find the clip
        Clip clip = clipRepository.findById(clipId)
                .orElseThrow(() -> new ApiException("Clip not found", HttpStatus.NOT_FOUND));

        // 2. Check if user already voted on this clip
        if (voteRepository.existsByUserIdAndClipId(
                user.getId(), clipId)) {
            throw new ApiException("You already voted on this clip", HttpStatus.CONFLICT);
        }

        // 3. Save vote to PostgreSQL
        Vote vote = Vote.builder()
                .user(user)
                .clip(clip)
                .build();
        voteRepository.save(vote);

        // 4. Update vote count in clips table
        clip.setVoteCount(clip.getVoteCount() + 1);
        clipRepository.save(clip);

        // 5. Update Redis leaderboard
        leaderboardService.incrementClipScore(
                clip.getDrop().getId(),
                clipId
        );
    }

    // ── Get leaderboard for a drop ────────────────────────

    public List<ClipResponseDTO> getLeaderboard(UUID dropId, int limit) {

        // 1. Get top clip IDs from Redis
        Set<String> topClipIds = leaderboardService
                .getTopClips(dropId, limit);

        if (topClipIds == null || topClipIds.isEmpty()) {
            return List.of();
        }

        // 2. Fetch clip details from PostgreSQL
        return topClipIds.stream()
                .map(clipId -> clipRepository
                        .findById(UUID.fromString(clipId))
                        .orElse(null))
                .filter(clip -> clip != null)
                .map(clip -> ClipResponseDTO.builder()
                        .id(clip.getId())
                        .title(clip.getTitle())
                        .thumbnailUrl(clip.getThumbnailUrl())
                        .status(clip.getStatus())
                        .voteCount(clip.getVoteCount())
                        .username(clip.getUser().getUsername())
                        .displayName(clip.getUser().getDisplayName())
                        .createdAt(clip.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}