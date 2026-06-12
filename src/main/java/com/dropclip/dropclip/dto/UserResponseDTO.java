package com.dropclip.dropclip.dto;

import com.dropclip.dropclip.entity.BadgeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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
    private List<BadgeType> badges;
}