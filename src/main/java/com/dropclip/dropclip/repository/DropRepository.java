package com.dropclip.dropclip.repository;

import com.dropclip.dropclip.entity.Drop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DropRepository extends JpaRepository<Drop, UUID> {

    Optional<Drop> findByIsActiveTrue();
}