package com.ekycrail.domain;

import java.util.Objects;

public record Nonce(String value) {

    public Nonce {
        requireValue(value);
    }

    public static Nonce of(String value) {
        return new Nonce(value);
    }

    private static void requireValue(String value) {
        Objects.requireNonNull(value, "nonce must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("nonce must not be blank");
        }
    }
}
