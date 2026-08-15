package com.ekycrail.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

public class ResilienceConfig {
    public CircuitBreakerConfig providerCircuitBreakerConfig() {
        throw new UnsupportedOperationException("Resilience configuration is not implemented yet");
    }

    public RetryConfig providerRetryConfig() {
        throw new UnsupportedOperationException("Resilience configuration is not implemented yet");
    }

    public TimeLimiterConfig providerTimeLimiterConfig() {
        throw new UnsupportedOperationException("Resilience configuration is not implemented yet");
    }

    public BulkheadConfig providerBulkheadConfig() {
        throw new UnsupportedOperationException("Resilience configuration is not implemented yet");
    }
}
