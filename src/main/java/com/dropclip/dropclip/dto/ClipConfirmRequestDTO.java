package com.dropclip.dropclip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClipConfirmRequestDTO {

    @NotBlank
    private String s3Key;

    private String title;
}