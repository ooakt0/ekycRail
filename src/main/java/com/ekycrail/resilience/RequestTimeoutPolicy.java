package com.ekycrail.resilience;

import com.ekycrail.domain.RequestDeadline;
import reactor.core.publisher.Mono;

public interface RequestTimeoutPolicy {
    <T> Mono<T> withDeadline(Mono<T> operation, RequestDeadline deadline);
}
