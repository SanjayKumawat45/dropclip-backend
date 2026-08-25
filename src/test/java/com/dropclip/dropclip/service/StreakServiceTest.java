package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.StreakResponseDTO;
import com.dropclip.dropclip.entity.Drop;
import com.dropclip.dropclip.entity.User;
import com.dropclip.dropclip.repository.ClipRepository;
import com.dropclip.dropclip.repository.DropRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private ClipRepository clipRepository;

    @Mock
    private DropRepository dropRepository;

    @InjectMocks
    private StreakService streakService;

    @Test
    void firstClip_setsStreakToOne() {
        User user = userWithStreak(0, 0);
        Drop drop = dropAt(LocalDateTime.of(2026, 6, 11, 0, 0));

        StreakResponseDTO result = streakService.updateStreakOnClipSubmit(user, drop);

        assertThat(user.getStreakCount()).isEqualTo(1);
        assertThat(result.getStreakCount()).isEqualTo(1);
        assertThat(result.getStreakExtended()).isFalse();
        assertThat(result.getStreakReset()).isFalse();
        assertThat(result.getSubmittedToday()).isTrue();
    }

    @Test
    void consecutiveDrop_incrementsStreak() {
        UUID userId = UUID.randomUUID();
        User user = userWithStreak(2, 2);
        user.setId(userId);

        Drop previousDrop = dropAt(LocalDateTime.of(2026, 6, 10, 0, 0));
        Drop currentDrop = dropAt(LocalDateTime.of(2026, 6, 11, 0, 0));

        when(dropRepository.findTopByStartsAtBeforeOrderByStartsAtDesc(currentDrop.getStartsAt()))
                .thenReturn(Optional.of(previousDrop));
        when(clipRepository.existsByUserIdAndDropId(userId, previousDrop.getId()))
                .thenReturn(true);

        StreakResponseDTO result = streakService.updateStreakOnClipSubmit(user, currentDrop);

        assertThat(user.getStreakCount()).isEqualTo(3);
        assertThat(result.getStreakExtended()).isTrue();
        assertThat(result.getStreakReset()).isFalse();
    }

    @Test
    void missedDrop_resetsStreakToOne() {
        UUID userId = UUID.randomUUID();
        User user = userWithStreak(5, 5);
        user.setId(userId);

        Drop previousDrop = dropAt(LocalDateTime.of(2026, 6, 10, 0, 0));
        Drop currentDrop = dropAt(LocalDateTime.of(2026, 6, 11, 0, 0));

        when(dropRepository.findTopByStartsAtBeforeOrderByStartsAtDesc(currentDrop.getStartsAt()))
                .thenReturn(Optional.of(previousDrop));
        when(clipRepository.existsByUserIdAndDropId(userId, previousDrop.getId()))
                .thenReturn(false);

        StreakResponseDTO result = streakService.updateStreakOnClipSubmit(user, currentDrop);

        assertThat(user.getStreakCount()).isEqualTo(1);
        assertThat(result.getStreakReset()).isTrue();
        assertThat(result.getStreakExtended()).isFalse();
    }

    @Test
    void getStreakStatus_reflectsSubmittedToday() {
        UUID userId = UUID.randomUUID();
        User user = userWithStreak(4, 3);
        user.setId(userId);

        Drop activeDrop = dropAt(LocalDateTime.of(2026, 6, 11, 0, 0));
        activeDrop.setId(UUID.randomUUID());

        when(dropRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeDrop));
        when(clipRepository.existsByUserIdAndDropId(userId, activeDrop.getId()))
                .thenReturn(true);

        StreakResponseDTO result = streakService.getStreakStatus(user);

        assertThat(result.getStreakCount()).isEqualTo(4);
        assertThat(result.getSubmittedToday()).isTrue();
    }

    private User userWithStreak(int streakCount, int totalClips) {
        return User.builder()
                .username("alice")
                .email("alice@test.com")
                .passwordHash("hash")
                .streakCount(streakCount)
                .totalClips(totalClips)
                .isActive(true)
                .build();
    }

    private Drop dropAt(LocalDateTime startsAt) {
        return Drop.builder()
                .title("Daily Drop")
                .promptText("Show your talent")
                .startsAt(startsAt)
                .expiresAt(startsAt.plusHours(24))
                .isActive(true)
                .build();
    }
}
