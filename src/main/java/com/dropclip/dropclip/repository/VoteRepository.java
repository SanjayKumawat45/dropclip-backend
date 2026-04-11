package com.dropclip.dropclip.repository;

import com.dropclip.dropclip.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {

    Boolean existsByUserIdAndClipId(UUID userId, UUID clipId);
}