package com.ekycrail.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogTest {

    @Test
    void shouldSetAndGetAllAllowedAuditMetadataFields() {
        Instant now = Instant.now();
        AuditLog auditLog = new AuditLog(
                "txn-123",
                "bank-abc",
                "NAGARIK_APP",
                "SUCCEEDED",
                "NONE",
                420L,
                now
        );

        assertEquals("txn-123", auditLog.getTransactionId());
        assertEquals("bank-abc", auditLog.getBankId());
        assertEquals("NAGARIK_APP", auditLog.getProviderType());
        assertEquals("SUCCEEDED", auditLog.getStatus());
        assertEquals("NONE", auditLog.getErrorCode());
        assertEquals(420L, auditLog.getLatencyMs());
        assertEquals(now, auditLog.getTimestamp());

        Instant updated = now.plusSeconds(60);
        auditLog.setStatus("FAILED");
        auditLog.setErrorCode("UPSTREAM_TIMEOUT");
        auditLog.setLatencyMs(1337L);
        auditLog.setTimestamp(updated);

        assertEquals("FAILED", auditLog.getStatus());
        assertEquals("UPSTREAM_TIMEOUT", auditLog.getErrorCode());
        assertEquals(1337L, auditLog.getLatencyMs());
        assertEquals(updated, auditLog.getTimestamp());
    }

    @Test
    void shouldContainOnlyNonPiiAuditFields() {
        Set<String> fieldNames = Arrays.stream(AuditLog.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        // "isNew" is a @Transient bookkeeping field for Persistable (not a DB column),
        // required so R2DBC issues INSERT (not UPDATE) for client-assigned IDs.
        assertEquals(
                Set.of("transactionId", "bankId", "providerType", "status", "errorCode", "latencyMs", "timestamp", "isNew"),
                fieldNames
        );
    }

    @Test
    void shouldBeNewByDefaultForFreshlyConstructedInstances() {
        AuditLog auditLog = new AuditLog(
                "txn-999",
                "bank-xyz",
                "NAGARIK_APP",
                "SUCCEEDED",
                "NONE",
                100L,
                Instant.now()
        );

        assertEquals("txn-999", auditLog.getId());
        org.junit.jupiter.api.Assertions.assertTrue(auditLog.isNew());

        auditLog.markNotNew();
        org.junit.jupiter.api.Assertions.assertFalse(auditLog.isNew());
    }
}

