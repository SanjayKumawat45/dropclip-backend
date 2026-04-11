package com.dropclip.dropclip.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "drops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drop extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 300)
    private String promptText;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isActive = true;
}