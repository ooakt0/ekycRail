package com.ekycrail.integration;

import com.ekycrail.domain.ProviderResult;
import com.ekycrail.domain.RequestDeadline;
import com.ekycrail.domain.VerificationTransaction;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public final class NagarikClient implements IdentityProviderClient {

    @Override
    public Mono<ProviderResult> verify(
            VerificationTransaction transaction,
            RequestDeadline deadline
    ) {
        return notImplemented();
    }

    @Override
    public Mono<Boolean> isReachable() {
        return notImplemented();
    }

    private Mono<ClientResponse> execute(
            NagarikProviderRequest request,
            RequestDeadline deadline
    ) {
        return notImplemented();
    }

    private Mono<NagarikProviderResponse> parseResponse(ClientResponse response) {
        return notImplemented();
    }

    private ProviderResult normalize(NagarikProviderResponse response, long latencyMs) {
        throw new UnsupportedOperationException("Nagarik provider payload contract is not implemented yet");
    }

    private <T> Mono<T> notImplemented() {
        return Mono.error(new UnsupportedOperationException("Nagarik provider client is not implemented yet"));
    }
}
