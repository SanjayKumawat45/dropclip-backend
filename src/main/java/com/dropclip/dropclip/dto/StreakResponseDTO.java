package com.dropclip.dropclip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class StreakResponseDTO {

    private Integer streakCount;
    private Boolean submittedToday;
    private Boolean streakExtended;
    private Boolean streakReset;
}
