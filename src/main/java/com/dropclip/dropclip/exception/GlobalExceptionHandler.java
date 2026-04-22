package com.dropclip.dropclip.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Handle our custom ApiException ────────────────────

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleApiException(
            ApiException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                ex.getStatus().value(),
                ex.getStatus().name(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(error);
    }

    // ── Handle @Valid validation failures ─────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ErrorResponseDTO error = new ErrorResponseDTO(
                400,
                "BAD_REQUEST",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    // ── Handle all other unexpected exceptions ────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
                500,
                "INTERNAL_SERVER_ERROR",
                "Something went wrong. Please try again.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}