package com.ekycrail.domain;

import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import com.ekycrail.support.CorrelationId;
import java.util.Objects;
import java.util.Set;

public record VerificationTransaction(
        TransactionId transactionId,
        TenantId tenantId,
        Purpose purpose,
        ConsentReference consentReference,
        Nonce nonce,
        Set<VerificationScope> scopes,
        CorrelationId correlationId
) {
    public VerificationTransaction {
        Objects.requireNonNull(transactionId, "transaction ID must not be null");
        Objects.requireNonNull(tenantId, "tenant ID must not be null");
        Objects.requireNonNull(purpose, "purpose must not be null");
        Objects.requireNonNull(consentReference, "consent reference must not be null");
        Objects.requireNonNull(nonce, "nonce must not be null");
        scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes must not be null"));
        Objects.requireNonNull(correlationId, "correlation ID must not be null");
    }
}
