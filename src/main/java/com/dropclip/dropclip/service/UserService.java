package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.StreakResponseDTO;
import com.dropclip.dropclip.dto.UserResponseDTO;
import com.dropclip.dropclip.dto.UserSearchResponseDTO;
import com.dropclip.dropclip.entity.Badge;
import com.dropclip.dropclip.entity.BadgeType;
import com.dropclip.dropclip.entity.User;
import com.dropclip.dropclip.exception.ApiException;
import com.dropclip.dropclip.repository.BadgeRepository;
import com.dropclip.dropclip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StreakService streakService;
    private final BadgeRepository badgeRepository;

    // =====================================================
    // CURRENT USER
    // =====================================================

    public User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    // =====================================================
    // MY PROFILE
    // =====================================================

    public UserResponseDTO getCurrentUserProfile() {
        return mapToUserResponse(getCurrentUser());
    }

    // =====================================================
    // MY STREAK
    // =====================================================

    public StreakResponseDTO getCurrentUserStreak() {
        return streakService.getStreakStatus(
                getCurrentUser()
        );
    }

    // =====================================================
    // PUBLIC USER PROFILE
    // =====================================================

    public UserResponseDTO getUserProfileByUsername(
            String username
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        )
                );

        return mapToUserResponse(user);
    }

    // =====================================================
    // MAP USER -> RESPONSE DTO
    // =====================================================

    private UserResponseDTO mapToUserResponse(
            User user
    ) {

        List<BadgeType> badges = badgeRepository
                .findByUserId(user.getId())
                .stream()
                .map(Badge::getBadgeType)
                .toList();

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .streakCount(user.getStreakCount())
                .totalClips(user.getTotalClips())
                .badges(badges)
                .build();
    }

    // =====================================================
    // SEARCH USERS
    // =====================================================

    public List<UserSearchResponseDTO> searchUsers(
            String query
    ) {

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String searchQuery = query.trim();

        return userRepository
                .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                        searchQuery,
                        searchQuery
                )
                .stream()
                .map(user ->
                        new UserSearchResponseDTO(
                                user.getId(),
                                user.getUsername(),
                                user.getDisplayName(),
                                user.getAvatarUrl()
                        )
                )
                .toList();
    }
}