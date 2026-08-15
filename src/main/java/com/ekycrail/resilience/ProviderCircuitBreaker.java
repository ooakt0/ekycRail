package com.ekycrail.resilience;

import com.ekycrail.domain.ProviderResult;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;

public interface ProviderCircuitBreaker {
    Mono<ProviderResult> execute(Supplier<Mono<ProviderResult>> operation);

    CircuitBreakerState state();
}
