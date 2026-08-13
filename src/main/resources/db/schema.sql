-- Zero-PII audit log table: stores only transaction metadata, never identity payloads.
CREATE TABLE IF NOT EXISTS audit_log (
    transaction_id  VARCHAR(128) PRIMARY KEY,
    bank_id         VARCHAR(64)  NOT NULL,
    provider_type   VARCHAR(64)  NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    error_code      VARCHAR(64),
    latency_ms      BIGINT,
    timestamp       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_bank_id ON audit_log (bank_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_status ON audit_log (status);

