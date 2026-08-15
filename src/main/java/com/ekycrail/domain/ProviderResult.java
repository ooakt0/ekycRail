package com.ekycrail.domain;

import com.ekycrail.enums.ProviderType;
import com.ekycrail.enums.ResultCode;
import com.ekycrail.enums.VerificationScope;
import java.util.Objects;
import java.util.Set;

public record ProviderResult(
        ProviderType provider,
        ResultCode resultCode,
        Set<VerificationScope> matchedScopes,
        long latencyMs,
        boolean fallbackOccurred,
        String providerReference
) {
    public ProviderResult {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(resultCode, "result code must not be null");
        matchedScopes = Set.copyOf(Objects.requireNonNull(matchedScopes, "matched scopes must not be null"));
        if (latencyMs < 0) {
            throw new IllegalArgumentException("latency must not be negative");
        }
        Objects.requireNonNull(providerReference, "provider reference must not be null");
    }

    public boolean isSuccessful() {
        return resultCode == ResultCode.SUCCEEDED;
    }

    public boolean isRetryable() {
        return switch (resultCode) {
            case UPSTREAM_TIMEOUT,
                    UPSTREAM_UNAVAILABLE,
                    PROVIDER_ERROR,
                    RATE_LIMITED_UPSTREAM,
                    SIGNING_FAILED,
                    AUDIT_WRITE_UNAVAILABLE -> true;
            default -> false;
        };
    }
}
