package com.ekycrail.domain;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record RequestDeadline(Instant deadline) {

    public RequestDeadline {
        Objects.requireNonNull(deadline, "deadline must not be null");
    }

    public static RequestDeadline from(Duration budget) {
        Objects.requireNonNull(budget, "budget must not be null");
        if (budget.isNegative()) {
            throw new IllegalArgumentException("budget must not be negative");
        }
        return new RequestDeadline(Instant.now().plus(budget));
    }

    public Duration remaining(Clock clock) {
        Objects.requireNonNull(clock, "clock must not be null");
        Duration remaining = Duration.between(clock.instant(), deadline);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
