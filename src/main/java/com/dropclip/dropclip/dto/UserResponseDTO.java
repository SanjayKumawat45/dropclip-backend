package com.dropclip.dropclip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Integer streakCount;
    private Integer totalClips;
}