package com.dropclip.dropclip.service;

import com.dropclip.dropclip.entity.Drop;
import com.dropclip.dropclip.entity.DropPrompt;
import com.dropclip.dropclip.repository.DropPromptRepository;
import com.dropclip.dropclip.repository.DropRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DropSchedulerService {

    private final DropRepository dropRepository;
    private final DropPromptRepository dropPromptRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void createDailyDrop() {
        log.info("🎬 Creating daily drop at midnight...");

        // 1. Deactivate current active drop
        dropRepository.findByIsActiveTrue()
                .ifPresent(currentDrop -> {
                    currentDrop.setIsActive(false);
                    dropRepository.save(currentDrop);

                    // Delete leaderboard from Redis
                    String leaderboardKey = "leaderboard:"
                            + currentDrop.getId().toString();
                    redisTemplate.delete(leaderboardKey);

                    log.info("✅ Deactivated drop: {}",
                            currentDrop.getTitle());
                });

        // 2. Get next unused prompt
        DropPrompt prompt = dropPromptRepository
                .findFirstByIsUsedFalse()
                .orElseGet(() -> {
                    // All prompts used — reset them all
                    log.info("🔄 All prompts used, resetting...");
                    dropPromptRepository.findAll()
                            .forEach(p -> {
                                p.setIsUsed(false);
                                dropPromptRepository.save(p);
                            });
                    return dropPromptRepository
                            .findFirstByIsUsedFalse()
                            .orElseThrow(() ->
                                    new RuntimeException("No prompts available"));
                });

        // 3. Create new drop
        Drop newDrop = Drop.builder()
                .title(prompt.getTitle())
                .promptText(prompt.getPromptText())
                .description("Today's daily challenge")
                .startsAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .isActive(true)
                .build();

        dropRepository.save(newDrop);

        // 4. Mark prompt as used
        prompt.setIsUsed(true);
        dropPromptRepository.save(prompt);

        log.info("✅ New drop created: {}", newDrop.getTitle());
    }

    // For testing — trigger drop creation manually
    @Scheduled(initialDelay = Long.MAX_VALUE, fixedDelay = Long.MAX_VALUE)
    public void testCreateDrop() {
        createDailyDrop();
    }
}