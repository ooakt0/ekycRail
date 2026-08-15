package com.ekycrail.security;

import com.ekycrail.domain.ProviderResult;
import com.ekycrail.domain.VerificationTransaction;
import java.time.Instant;

public interface JwtClaimsFactory {
    VerificationAssertionClaims create(
            VerificationTransaction transaction,
            ProviderResult result,
            Instant issuedAt,
            Instant expiresAt
    );
}
