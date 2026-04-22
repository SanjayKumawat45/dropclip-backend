package com.dropclip.dropclip.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ErrorResponseDTO {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ErrorResponseDTO(int status, String error,
                            String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}