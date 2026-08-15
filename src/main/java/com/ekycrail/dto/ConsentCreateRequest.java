package com.ekycrail.dto;

import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Set;

public record ConsentCreateRequest(
        String bankId,
        Purpose purpose,
        Set<VerificationScope> scopes,
        String consentReceiptId,
        Instant expiresAt,
        String dataSharingAgreementVersion
) {
    public ConsentCreateRequest {
        scopes = Set.copyOf(scopes);
    }
}
