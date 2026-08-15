package com.ekycrail.security;

import com.ekycrail.enums.ResultCode;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Set;

public record VerificationAssertionClaims(
        String issuer,
        String audience,
        String transactionId,
        ResultCode resultCode,
        Set<VerificationScope> scopes,
        Instant issuedAt,
        Instant expiresAt,
        String jti
) {
    public VerificationAssertionClaims {
        scopes = Set.copyOf(scopes);
    }
}
