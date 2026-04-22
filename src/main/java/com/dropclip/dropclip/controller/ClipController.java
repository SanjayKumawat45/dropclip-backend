package com.dropclip.dropclip.controller;

import com.dropclip.dropclip.dto.*;
import com.dropclip.dropclip.service.ClipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clips")
@RequiredArgsConstructor
public class ClipController {

    private final ClipService clipService;

    @GetMapping("/presign")
    public ResponseEntity<PresignedUrlResponseDTO> getPresignedUrl() {
        return ResponseEntity.ok(clipService.generatePresignedUrl());
    }

    @PostMapping("/confirm")
    public ResponseEntity<ClipResponseDTO> confirmUpload(
            @Valid @RequestBody ClipConfirmRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clipService.confirmUpload(request));
    }

    @GetMapping
    public ResponseEntity<List<ClipResponseDTO>> getClips() {
        return ResponseEntity.ok(clipService.getClipsForActiveDrop());
    }
}