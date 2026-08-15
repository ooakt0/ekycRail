package com.ekycrail.domain;

import com.ekycrail.enums.ProviderType;
import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.ResultCode;
import com.ekycrail.enums.VerificationScope;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class AuditRecord {
    private Long id;
    private String transactionId;
    private String bankId;
    private ProviderType provider;
    private Purpose purpose;
    private Set<VerificationScope> scopes;
    private ResultCode resultCode;
    private long latencyMs;
    private boolean fallbackOccurred;
    private Instant createdAt;

    public AuditRecord() {
    }

    private AuditRecord(
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
        this.transactionId = requireText(transactionId, "transaction ID");
        this.bankId = requireText(bankId, "bank ID");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.purpose = Objects.requireNonNull(purpose, "purpose must not be null");
        this.scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes must not be null"));
        this.resultCode = Objects.requireNonNull(resultCode, "result code must not be null");
        if (latencyMs < 0) {
            throw new IllegalArgumentException("latency must not be negative");
        }
        this.latencyMs = latencyMs;
        this.fallbackOccurred = fallbackOccurred;
        this.createdAt = Objects.requireNonNull(createdAt, "created at must not be null");
    }

    public static AuditRecord success(
            String transactionId,
            String bankId,
            ProviderType provider,
            Purpose purpose,
            Set<VerificationScope> scopes,
            long latencyMs,
            boolean fallbackOccurred,
            Instant createdAt
    ) {
        return new AuditRecord(
                transactionId,
                bankId,
                provider,
                purpose,
                scopes,
                ResultCode.SUCCEEDED,
                latencyMs,
                fallbackOccurred,
                createdAt
        );
    }

    public static AuditRecord failure(
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
        if (resultCode == ResultCode.SUCCEEDED) {
            throw new IllegalArgumentException("a failure audit record must not use SUCCEEDED");
        }
        return new AuditRecord(
                transactionId,
                bankId,
                provider,
                purpose,
                scopes,
                resultCode,
                latencyMs,
                fallbackOccurred,
                createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getBankId() {
        return bankId;
    }

    public ProviderType getProvider() {
        return provider;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public Set<VerificationScope> getScopes() {
        return scopes == null ? Set.of() : Set.copyOf(scopes);
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public boolean isFallbackOccurred() {
        return fallbackOccurred;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
