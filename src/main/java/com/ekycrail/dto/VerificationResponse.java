package com.ekycrail.dto;

import com.ekycrail.enums.ResultCode;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Set;

public record VerificationResponse(
        String transactionId,
        ResultCode resultCode,
        Set<VerificationScope> matchedScopes,
        String verificationId,
        String jwt,
        long latencyMs,
        boolean fallbackOccurred,
        Instant expiresAt
) {
    public VerificationResponse {
        matchedScopes = Set.copyOf(matchedScopes);
    }
}
