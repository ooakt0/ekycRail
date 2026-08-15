package com.ekycrail.domain;

import java.util.Objects;

public record TransactionId(String value) {

    public TransactionId {
        requireValue(value, "transaction ID");
    }

    public static TransactionId of(String value) {
        return new TransactionId(value);
    }

    private static void requireValue(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
    }
}
