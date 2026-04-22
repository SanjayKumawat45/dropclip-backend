package com.dropclip.dropclip.repository;

import com.dropclip.dropclip.entity.DropPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DropPromptRepository extends JpaRepository<DropPrompt, UUID> {

    Optional<DropPrompt> findFirstByIsUsedFalse();
}