package com.ekycrail.support;

import java.util.Objects;
import java.util.UUID;

public record CorrelationId(String value) {

    public CorrelationId {
        Objects.requireNonNull(value, "correlation ID must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("correlation ID must not be blank");
        }
    }

    public static CorrelationId generate() {
        return new CorrelationId(UUID.randomUUID().toString());
    }
}
