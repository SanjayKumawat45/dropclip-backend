package com.dropclip.dropclip.controller;

import com.dropclip.dropclip.entity.Drop;
import com.dropclip.dropclip.repository.DropRepository;
import com.dropclip.dropclip.service.DropSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drops")
@RequiredArgsConstructor
public class DropController {

    private final DropRepository dropRepository;
    private final DropSchedulerService dropSchedulerService;

    @GetMapping("/active")
    public ResponseEntity<Drop> getActiveDrop() {
        return dropRepository.findByIsActiveTrue()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerDrop() {
        dropSchedulerService.createDailyDrop();
        return ResponseEntity.ok("New drop created successfully!");
    }
}