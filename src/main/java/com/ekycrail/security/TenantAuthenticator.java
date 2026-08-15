package com.ekycrail.security;

import com.ekycrail.dto.TenantContext;
import java.security.cert.X509Certificate;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public final class TenantAuthenticator {
    public Mono<TenantContext> authenticate(ServerRequest request) {
        return notImplemented();
    }

    public Mono<TenantContext> authenticateCertificate(X509Certificate certificate) {
        return notImplemented();
    }

    public Mono<TenantContext> authenticateSignedRequest(
            String apiKey,
            String signature,
            byte[] canonicalRequest
    ) {
        return notImplemented();
    }

    private <T> Mono<T> notImplemented() {
        return Mono.error(new UnsupportedOperationException("Tenant authentication is not implemented yet"));
    }
}
