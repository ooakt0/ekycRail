package com.ekycrail.dto;

import com.ekycrail.enums.ProviderType;
import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.ResultCode;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Set;

public record AuditMetadataResponse(
        String transactionId,
        String bankId,
        ProviderType provider,
        Purpose purpose,
        Set<VerificationScope> scopes,
        ResultCode resultCode,
        long latencyMs,
        boolean fallbackOccurred,
        Instant createdAt
) {
    public AuditMetadataResponse {
        scopes = Set.copyOf(scopes);
    }
}
