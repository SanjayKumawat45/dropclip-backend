package com.dropclip.dropclip.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "drop_prompts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 300)
    private String promptText;

    @Column(nullable = false)
    private Boolean isUsed = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}