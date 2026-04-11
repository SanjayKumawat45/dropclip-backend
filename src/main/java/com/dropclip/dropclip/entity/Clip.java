package com.dropclip.dropclip.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drop_id", nullable = false)
    private Drop drop;

    @Column(length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String s3Key;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false, length = 20)
    private String status = "PROCESSING";

    @Column(nullable = false)
    private Integer voteCount = 0;
}