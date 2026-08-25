package com.dropclip.dropclip.entity;

import jakarta.persistence.*;
import lombok.*;
import com.dropclip.dropclip.entity.BadgeType;

import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BadgeType badgeType;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;
}