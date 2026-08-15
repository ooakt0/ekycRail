package com.ekycrail.integration;

import com.ekycrail.domain.ProviderResult;
import com.ekycrail.domain.RequestDeadline;
import com.ekycrail.domain.VerificationTransaction;
import reactor.core.publisher.Mono;

public interface IdentityProviderClient {
    Mono<ProviderResult> verify(
            VerificationTransaction transaction,
            RequestDeadline deadline
    );

    Mono<Boolean> isReachable();
}
