package com.ekycrail.repository;

import com.ekycrail.domain.AuditRecord;
import java.time.Instant;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuditRepository {
    Mono<AuditRecord> insert(AuditRecord record);

    Mono<Optional<AuditRecord>> findByTransactionId(String transactionId);

    Flux<AuditRecord> findByBankIdAndCreatedAtBetween(String bankId, Instant from, Instant to);
}
