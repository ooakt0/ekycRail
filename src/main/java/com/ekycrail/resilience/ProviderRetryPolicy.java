package com.ekycrail.resilience;

import java.util.function.Supplier;
import reactor.core.publisher.Mono;

public interface ProviderRetryPolicy {
    <T> Mono<T> execute(Supplier<Mono<T>> operation);
}
