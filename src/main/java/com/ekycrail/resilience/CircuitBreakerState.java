package com.ekycrail.resilience;

public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN,
    DISABLED,
    FORCED_OPEN,
    METRICS_ONLY
}
