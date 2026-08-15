package com.ekycrail.security;

import com.nimbusds.jose.jwk.JWKSet;
import java.security.PublicKey;
import reactor.core.publisher.Mono;

public interface JwksService {
    Mono<JWKSet> getJwks();

    Mono<PublicKey> getActivePublicKey();

    Mono<String> getActiveKeyId();
}
