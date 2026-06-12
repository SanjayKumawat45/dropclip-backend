package com.dropclip.dropclip.service;

import com.dropclip.dropclip.entity.Badge;
import com.dropclip.dropclip.entity.BadgeType;
import com.dropclip.dropclip.entity.User;
import com.dropclip.dropclip.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public void evaluateBadges(User user) {

        // First upload badge
        if (user.getTotalClips() >= 1) {
            awardBadge(user, BadgeType.FIRST_DROP);
        }

        // 3-drop streak badge
        if (user.getStreakCount() >= 3) {
            awardBadge(user, BadgeType.ON_FIRE);
        }

        // 7-drop streak badge
        if (user.getStreakCount() >= 7) {
            awardBadge(user, BadgeType.UNSTOPPABLE);
        }
    }

    public void awardCrowdFavourite(User user) {
        awardBadge(user, BadgeType.CROWD_FAVOURITE);
    }

    private void awardBadge(User user, BadgeType badgeType) {

        boolean alreadyAwarded =
                badgeRepository.existsByUserIdAndBadgeType(
                        user.getId(),
                        badgeType
                );

        if (alreadyAwarded) {
            return;
        }

        Badge badge = Badge.builder()
                .user(user)
                .badgeType(badgeType)
                .earnedAt(LocalDateTime.now())
                .build();

        badgeRepository.save(badge);
    }
}