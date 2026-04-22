package com.dropclip.dropclip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LEADERBOARD_KEY = "leaderboard:";

    // ── Add clip to leaderboard ───────────────────────────

    public void addClipToLeaderboard(UUID dropId, UUID clipId) {
        String key = LEADERBOARD_KEY + dropId.toString();
        redisTemplate.opsForZSet().add(key, clipId.toString(), 0);
    }

    // ── Increment clip score by 1 when voted ─────────────

    public void incrementClipScore(UUID dropId, UUID clipId) {
        String key = LEADERBOARD_KEY + dropId.toString();
        redisTemplate.opsForZSet().incrementScore(
                key, clipId.toString(), 1);
    }

    // ── Get top N clips for a drop ────────────────────────

    public Set<String> getTopClips(UUID dropId, int limit) {
        String key = LEADERBOARD_KEY + dropId.toString();
        return redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
    }

    // ── Get score of a specific clip ──────────────────────

    public Double getClipScore(UUID dropId, UUID clipId) {
        String key = LEADERBOARD_KEY + dropId.toString();
        return redisTemplate.opsForZSet().score(key, clipId.toString());
    }

    // ── Get rank of a clip (0 = top) ──────────────────────

    public Long getClipRank(UUID dropId, UUID clipId) {
        String key = LEADERBOARD_KEY + dropId.toString();
        return redisTemplate.opsForZSet().reverseRank(
                key, clipId.toString());
    }
}