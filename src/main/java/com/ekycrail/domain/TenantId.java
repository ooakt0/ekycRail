package com.ekycrail.domain;

import java.util.Objects;

public record TenantId(String value) {

    public TenantId {
        requireValue(value);
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }

    private static void requireValue(String value) {
        Objects.requireNonNull(value, "tenant ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("tenant ID must not be blank");
        }
    }
}
