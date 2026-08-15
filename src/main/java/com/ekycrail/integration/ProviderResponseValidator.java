package com.ekycrail.integration;

import java.util.Objects;

public final class ProviderResponseValidator {

    public void validate(NagarikProviderResponse response) {
        Objects.requireNonNull(response, "provider response must not be null");
    }
}
