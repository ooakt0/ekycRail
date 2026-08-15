package com.ekycrail.dto;

import com.ekycrail.domain.TenantId;
import com.ekycrail.enums.Purpose;
import java.util.Objects;
import java.util.Set;

public record TenantContext(
        TenantId tenantId,
        Set<String> scopes,
        Set<Purpose> permittedPurposes,
        String correlationId
) {
    public TenantContext {
        Objects.requireNonNull(tenantId, "tenant ID must not be null");
        scopes = Set.copyOf(Objects.requireNonNull(scopes, "scopes must not be null"));
        permittedPurposes = Set.copyOf(Objects.requireNonNull(permittedPurposes, "permitted purposes must not be null"));
        Objects.requireNonNull(correlationId, "correlation ID must not be null");
        if (correlationId.isBlank()) {
            throw new IllegalArgumentException("correlation ID must not be blank");
        }
    }
}
