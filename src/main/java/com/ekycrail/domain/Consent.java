package com.ekycrail.domain;

import com.ekycrail.enums.ConsentStatus;
import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Consent {
    private Long id;
    private String consentId;
    private String bankId;
    private Purpose purpose;
    private Set<VerificationScope> scopes;
    private String consentReceiptId;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String dataSharingAgreementVersion;

    public Consent() {
    }

    public Consent(
            String consentId,
            String bankId,
            Purpose purpose,
            Set<VerificationScope> scopes,
            String consentReceiptId,
            Instant createdAt,
            Instant expiresAt,
            Instant revokedAt,
            String dataSharingAgreementVersion
    ) {
        this.consentId = requireText(consentId, "consent ID");
        this.bankId = requireText(bankId, "bank ID");
        this.purpose = Objects.requireNonNull(purpose, "purpose must not be null");
        this.scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes must not be null"));
        this.consentReceiptId = requireText(consentReceiptId, "consent receipt ID");
        this.createdAt = Objects.requireNonNull(createdAt, "created at must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expires at must not be null");
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiry must be after creation");
        }
        this.revokedAt = revokedAt;
        this.dataSharingAgreementVersion = requireText(dataSharingAgreementVersion, "data-sharing agreement version");
    }

    public boolean isActive(Instant now) {
        Objects.requireNonNull(now, "current time must not be null");
        return !now.isBefore(createdAt) && !isExpired(now) && !isRevoked();
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "current time must not be null");
        return !now.isBefore(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean covers(Set<VerificationScope> requestedScopes) {
        return scopes.containsAll(Objects.requireNonNull(requestedScopes, "requested scopes must not be null"));
    }

    public ConsentStatus status(Instant now) {
        if (isRevoked()) {
            return ConsentStatus.REVOKED;
        }
        return isExpired(now) ? ConsentStatus.EXPIRED : ConsentStatus.ACTIVE;
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
