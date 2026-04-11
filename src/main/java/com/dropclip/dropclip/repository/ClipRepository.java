package com.dropclip.dropclip.repository;

import com.dropclip.dropclip.entity.Clip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClipRepository extends JpaRepository<Clip, UUID> {

    Boolean existsByUserIdAndDropId(UUID userId, UUID dropId);

    @Query("SELECT c FROM Clip c JOIN FETCH c.user WHERE c.drop.id = :dropId ORDER BY c.voteCount DESC")
    List<Clip> findClipsWithUserByDropId(@Param("dropId") UUID dropId);

    Optional<Clip> findByUserIdAndDropId(UUID userId, UUID dropId);
}