package com.ekycrail.repository;

import com.ekycrail.dto.VerificationResponse;
import java.time.Instant;
import java.util.Optional;
import reactor.core.publisher.Mono;

public interface IdempotencyRepository {
    Mono<Optional<VerificationResponse>> findByTransactionId(String transactionId);

    Mono<Boolean> insertReservation(String transactionId, Instant expiresAt);

    Mono<Boolean> saveResult(
            String transactionId,
            VerificationResponse response,
            Instant completedAt
    );
}
