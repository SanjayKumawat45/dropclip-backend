package com.dropclip.dropclip.repository;

import com.dropclip.dropclip.entity.Badge;
import com.dropclip.dropclip.entity.BadgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {

    List<Badge> findByUserId(UUID userId);

    boolean existsByUserIdAndBadgeType(UUID userId, BadgeType badgeType);
}