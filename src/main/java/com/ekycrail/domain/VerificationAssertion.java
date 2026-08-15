package com.ekycrail.domain;

import com.ekycrail.enums.ResultCode;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record VerificationAssertion(
        String transactionId,
        ResultCode resultCode,
        Set<VerificationScope> scopes,
        Instant issuedAt,
        Instant expiresAt,
        String jwt
) {
    public VerificationAssertion {
        Objects.requireNonNull(transactionId, "transaction ID must not be null");
        Objects.requireNonNull(resultCode, "result code must not be null");
        scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes must not be null"));
        Objects.requireNonNull(issuedAt, "issued at must not be null");
        Objects.requireNonNull(expiresAt, "expires at must not be null");
        Objects.requireNonNull(jwt, "JWT must not be null");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiry must be after issuance");
        }
    }
}
