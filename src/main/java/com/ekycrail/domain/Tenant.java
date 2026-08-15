package com.ekycrail.domain;

import com.ekycrail.enums.Purpose;
import java.util.Objects;
import java.util.Set;

public final class Tenant {
    private String tenantId;
    private String legalName;
    private boolean active;
    private Set<Purpose> permittedPurposes;
    private Set<String> permittedScopes;
    private int rateLimitPerSecond;

    public Tenant() {
    }

    public Tenant(
            String tenantId,
            String legalName,
            boolean active,
            Set<Purpose> permittedPurposes,
            Set<String> permittedScopes,
            int rateLimitPerSecond
    ) {
        this.tenantId = requireText(tenantId, "tenant ID");
        this.legalName = requireText(legalName, "legal name");
        this.active = active;
        this.permittedPurposes = Set.copyOf(Objects.requireNonNull(permittedPurposes, "permitted purposes must not be null"));
        this.permittedScopes = Set.copyOf(Objects.requireNonNull(permittedScopes, "permitted scopes must not be null"));
        if (rateLimitPerSecond < 0) {
            throw new IllegalArgumentException("rate limit must not be negative");
        }
        this.rateLimitPerSecond = rateLimitPerSecond;
    }

    public boolean isActive() {
        return active;
    }

    public boolean permits(Purpose purpose) {
        return permittedPurposes.contains(Objects.requireNonNull(purpose, "purpose must not be null"));
    }

    public boolean permitsScopes(Set<String> scopes) {
        return permittedScopes.containsAll(Objects.requireNonNull(scopes, "scopes must not be null"));
    }

    public int rateLimitPerSecond() {
        return rateLimitPerSecond;
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
