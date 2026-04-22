package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.*;
import com.dropclip.dropclip.entity.*;
import com.dropclip.dropclip.repository.*;
import lombok.RequiredArgsConstructor;
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

    // ── Get current logged in user ────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Generate presigned upload URL ─────────────────────

    public PresignedUrlResponseDTO generatePresignedUrl() {

        User user = getCurrentUser();

        // Get the active drop
        Drop drop = dropRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("No active drop right now"));

        // Check if user already submitted a clip for this drop
        if (clipRepository.existsByUserIdAndDropId(
                user.getId(), drop.getId())) {
            throw new RuntimeException(
                    "You already submitted a clip for today's drop");
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
                .orElseThrow(() -> new RuntimeException("No active drop"));

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

        // Update user's total clips count
        user.setTotalClips(user.getTotalClips() + 1);
        userRepository.save(user);

        return mapToClipResponse(clip);
    }

    // ── Get all clips for active drop ─────────────────────

    public List<ClipResponseDTO> getClipsForActiveDrop() {

        Drop drop = dropRepository.findByIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("No active drop"));

        return clipRepository
                .findClipsWithUserByDropId(drop.getId())
                .stream()
                .map(this::mapToClipResponse)
                .collect(Collectors.toList());
    }

    // ── Helper — map Clip entity to DTO ───────────────────

    private ClipResponseDTO mapToClipResponse(Clip clip) {
        return ClipResponseDTO.builder()
                .id(clip.getId())
                .title(clip.getTitle())
                .thumbnailUrl(clip.getThumbnailUrl())
                .clipUrl(s3Service.getClipUrl(clip.getS3Key()))
                .status(clip.getStatus())
                .voteCount(clip.getVoteCount())
                .username(clip.getUser().getUsername())
                .displayName(clip.getUser().getDisplayName())
                .createdAt(clip.getCreatedAt())
                .build();
    }
}