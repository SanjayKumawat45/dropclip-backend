package com.dropclip.dropclip.service;

import com.dropclip.dropclip.dto.StreakResponseDTO;
import com.dropclip.dropclip.entity.Drop;
import com.dropclip.dropclip.entity.User;
import com.dropclip.dropclip.repository.ClipRepository;
import com.dropclip.dropclip.repository.DropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final ClipRepository clipRepository;
    private final DropRepository dropRepository;

    /**
     * Updates the user's streak after they submit a clip for the current drop.
     * A streak continues when the user also submitted a clip for the immediately
     * previous drop (by starts_at). Otherwise the streak resets to 1.
     */
    public StreakResponseDTO updateStreakOnClipSubmit(User user, Drop currentDrop) {

        int previousStreak = user.getStreakCount();
        boolean firstClip = user.getTotalClips() == 0;

        int newStreak = calculateNewStreak(user.getId(), currentDrop, previousStreak, firstClip);

        user.setStreakCount(newStreak);

        boolean extended = previousStreak >= 1 && newStreak == previousStreak + 1;
        boolean reset = !firstClip && newStreak == 1 && previousStreak > 1;

        return StreakResponseDTO.builder()
                .streakCount(newStreak)
                .submittedToday(true)
                .streakExtended(extended)
                .streakReset(reset)
                .build();
    }

    public StreakResponseDTO getStreakStatus(User user) {

        boolean submittedToday = dropRepository.findByIsActiveTrue()
                .map(drop -> clipRepository.existsByUserIdAndDropId(user.getId(), drop.getId()))
                .orElse(false);

        return StreakResponseDTO.builder()
                .streakCount(user.getStreakCount())
                .submittedToday(submittedToday)
                .streakExtended(false)
                .streakReset(false)
                .build();
    }

    private int calculateNewStreak(
            UUID userId,
            Drop currentDrop,
            int previousStreak,
            boolean firstClip) {

        if (firstClip) {
            return 1;
        }

        return dropRepository
                .findTopByStartsAtBeforeOrderByStartsAtDesc(currentDrop.getStartsAt())
                .filter(previousDrop -> clipRepository.existsByUserIdAndDropId(
                        userId, previousDrop.getId()))
                .map(previousDrop -> previousStreak + 1)
                .orElse(1);
    }
}
