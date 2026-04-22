package com.dropclip.dropclip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponseDTO {
    private String presignedUrl;
    private String s3Key;
}