package com.ekycrail.dto;

import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import java.util.Set;

public record VerificationRequest(
        String transactionId,
        String bankId,
        Purpose purpose,
        String consentReference,
        Set<VerificationScope> scopes,
        String nonce,
        String correlationId
) {
    public VerificationRequest {
        scopes = Set.copyOf(scopes);
    }
}
