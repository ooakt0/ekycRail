package com.ekycrail.domain;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Zero-PII audit log entity. Implements {@link Persistable} because the
 * primary key ({@code transactionId}) is client-assigned rather than
 * database-generated. Without this, Spring Data R2DBC's default isNew()
 * check (based on a null ID) incorrectly treats every save() as an UPDATE,
 * which fails since the row does not yet exist.
 */
@Table("audit_log")
public class AuditLog implements Persistable<String> {

    @Id
    @Column("transaction_id")
    private String transactionId;

    @Column("bank_id")
    private String bankId;

    @Column("provider_type")
    private String providerType;

    @Column("status")
    private String status;

    @Column("error_code")
    private String errorCode;

    @Column("latency_ms")
    private Long latencyMs;

    @Column("timestamp")
    private Instant timestamp;

    // Not persisted; defaults to true so freshly constructed instances are always inserted.
    @Transient
    private boolean isNew = true;

    public AuditLog() {
    }

    public AuditLog(
            String transactionId,
            String bankId,
            String providerType,
            String status,
            String errorCode,
            Long latencyMs,
            Instant timestamp
    ) {
        this.transactionId = transactionId;
        this.bankId = bankId;
        this.providerType = providerType;
        this.status = status;
        this.errorCode = errorCode;
        this.latencyMs = latencyMs;
        this.timestamp = timestamp;
        this.isNew = true;
    }

    @Override
    public String getId() {
        return transactionId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    /**
     * Marks this instance as already persisted (e.g. after being read back from the
     * database via a repository query). Not typically needed for the write-only
     * audit trail flow, but provided for correctness/testability.
     */
    public AuditLog markNotNew() {
        this.isNew = false;
        return this;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

