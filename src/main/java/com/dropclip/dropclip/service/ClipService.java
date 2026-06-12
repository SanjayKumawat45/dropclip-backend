package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.*;
import com.dropclip.dropclip.entity.*;
import com.dropclip.dropclip.exception.ApiException;
import com.dropclip.dropclip.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClipService {

    private final ClipRepository clipRepository;
    private final UserRepository userRepository;
    private final DropRepository dropRepository;
    private final S3Service s3Service;
    private final LeaderboardService leaderboardService;
    private final StreakService streakService;
    private final BadgeService badgeService;

    // ── Get current logged in user ────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    // ── Generate presigned upload URL ─────────────────────

    public PresignedUrlResponseDTO generatePresignedUrl() {

        User user = getCurrentUser();

        // Get the active drop
        Drop drop = dropRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ApiException(
                        "No active drop right now", HttpStatus.NOT_FOUND));

        // Check if user already submitted a clip for this drop
        if (clipRepository.existsByUserIdAndDropId(
                user.getId(), drop.getId())) {
            throw new ApiException(
                    "You already submitted a clip for today's drop",
                    HttpStatus.CONFLICT);
        }

        // Generate presigned URL
        String presignedUrl = s3Service.generatePresignedUploadUrl(
                user.getId().toString(),
                drop.getId().toString()
        );

        // Extract s3Key from the presigned URL
        String s3Key = s3Service.extractS3Key(presignedUrl);

        return new PresignedUrlResponseDTO(presignedUrl, s3Key);
    }

    // ── Confirm upload and save clip ──────────────────────

    public ClipResponseDTO confirmUpload(ClipConfirmRequestDTO request) {

        User user = getCurrentUser();

        Drop drop = dropRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ApiException(
                        "No active drop", HttpStatus.NOT_FOUND));

        if (clipRepository.existsByUserIdAndDropId(user.getId(), drop.getId())) {
            throw new ApiException(
                    "You already submitted a clip for today's drop",
                    HttpStatus.CONFLICT);
        }

        var streakResult = streakService.updateStreakOnClipSubmit(user, drop);

        // Save clip to database
        Clip clip = Clip.builder()
                .user(user)
                .drop(drop)
                .title(request.getTitle())
                .s3Key(request.getS3Key())
                .status("READY")
                .voteCount(0)
                .build();

        clipRepository.save(clip);

        // Add to Redis leaderboard
        leaderboardService.addClipToLeaderboard(
                drop.getId(),
                clip.getId()
        );

        user.setTotalClips(user.getTotalClips() + 1);

        badgeService.evaluateBadges(user);

        userRepository.save(user);

        return mapToClipResponse(clip, streakResult);
    }

    // ── Get all clips for active drop ─────────────────────

    public List<ClipResponseDTO> getClipsForActiveDrop() {

        Drop drop = dropRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ApiException(
                        "No active drop", HttpStatus.NOT_FOUND));

        return clipRepository
                .findClipsWithUserByDropId(drop.getId())
                .stream()
                .map(clip -> mapToClipResponse(clip, null))
                .collect(Collectors.toList());
    }

    // ── Helper — map Clip entity to DTO ───────────────────

    private ClipResponseDTO mapToClipResponse(Clip clip, StreakResponseDTO streak) {
        ClipResponseDTO.ClipResponseDTOBuilder builder = ClipResponseDTO.builder()
                .id(clip.getId())
                .title(clip.getTitle())
                .thumbnailUrl(clip.getThumbnailUrl())
                .clipUrl(s3Service.getClipUrl(clip.getS3Key()))
                .status(clip.getStatus())
                .voteCount(clip.getVoteCount())
                .username(clip.getUser().getUsername())
                .displayName(clip.getUser().getDisplayName())
                .createdAt(clip.getCreatedAt());

        if (streak != null) {
            builder
                    .streakCount(streak.getStreakCount())
                    .streakExtended(streak.getStreakExtended())
                    .streakReset(streak.getStreakReset());
        }

        return builder.build();
    }
}