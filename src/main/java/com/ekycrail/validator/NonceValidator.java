package com.ekycrail.validator;

import reactor.core.publisher.Mono;

public interface NonceValidator {
    Mono<Void> requireUnused(String tenantId, String nonce);
}
