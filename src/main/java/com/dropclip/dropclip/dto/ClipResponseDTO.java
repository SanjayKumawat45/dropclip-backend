package com.dropclip.dropclip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ClipResponseDTO {
    private UUID id;
    private String title;
    private String thumbnailUrl;
    private String clipUrl;
    private String status;
    private Integer voteCount;
    private String username;
    private String displayName;
    private LocalDateTime createdAt;
    private Integer streakCount;
    private Boolean streakExtended;
    private Boolean streakReset;
}