package com.ekycrail.dto;

public record ConsentRevokeRequest(
        String reason,
        String actor
) {
}
