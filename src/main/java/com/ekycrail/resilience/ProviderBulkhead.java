package com.ekycrail.resilience;

import java.util.function.Supplier;
import reactor.core.publisher.Mono;

public interface ProviderBulkhead {
    <T> Mono<T> execute(Supplier<Mono<T>> operation);
}
