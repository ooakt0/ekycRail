package com.ekycrail.dto;

public record ErrorResponse(
        String errorCode,
        String message,
        String correlationId
) {
}
