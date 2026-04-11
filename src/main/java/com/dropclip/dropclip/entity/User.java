package com.dropclip.dropclip.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 300)
    private String bio;

    @Column(nullable = false)
    private Integer streakCount = 0;

    @Column(nullable = false)
    private Integer totalClips = 0;

    @Column(nullable = false)
    private Boolean isActive = true;
}