package com.ekycrail.repository;

import com.ekycrail.domain.Consent;
import com.ekycrail.enums.ConsentStatus;
import java.time.Instant;
import java.util.Optional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ConsentRepository {
    Mono<Consent> insert(Consent consent);

    Mono<Optional<Consent>> findByConsentId(String consentId);

    Mono<Optional<Consent>> findActiveForVerification(String bankId, String consentId, Instant now);

    Mono<Void> revoke(String consentId, String actor, String reason, Instant revokedAt);

    Flux<Consent> findByBankIdAndStatus(String bankId, ConsentStatus status);
}
