package com.ekycrail.domain;

import com.ekycrail.enums.VerificationScope;
import java.util.Objects;
import java.util.Set;

public record VerificationScopes(Set<VerificationScope> values) {

    public VerificationScopes {
        values = Set.copyOf(Objects.requireNonNull(values, "verification scopes must not be null"));
    }

    public static VerificationScopes of(Set<VerificationScope> values) {
        return new VerificationScopes(values);
    }

    public boolean containsAll(Set<VerificationScope> requested) {
        return values.containsAll(Objects.requireNonNull(requested, "requested scopes must not be null"));
    }
}
