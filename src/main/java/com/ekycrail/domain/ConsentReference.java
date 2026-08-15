package com.ekycrail.domain;

import java.util.Objects;

public record ConsentReference(String value) {

    public ConsentReference {
        requireValue(value);
    }

    public static ConsentReference of(String value) {
        return new ConsentReference(value);
    }

    private static void requireValue(String value) {
        Objects.requireNonNull(value, "consent reference must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("consent reference must not be blank");
        }
    }
}
