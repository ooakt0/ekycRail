package com.ekycrail.service;

import com.ekycrail.dto.VerificationResponse;
import java.util.Optional;
import reactor.core.publisher.Mono;

public interface IdempotencyService {
    Mono<Optional<VerificationResponse>> findCached(String transactionId);

    Mono<Void> reserve(String transactionId);

    Mono<Void> store(String transactionId, VerificationResponse response);
}
