package com.ekycrail.dto;

import com.ekycrail.enums.ConsentStatus;
import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Set;

public record ConsentResponse(
        String consentId,
        String bankId,
        Purpose purpose,
        Set<VerificationScope> scopes,
        ConsentStatus status,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt
) {
    public ConsentResponse {
        scopes = Set.copyOf(scopes);
    }
}
