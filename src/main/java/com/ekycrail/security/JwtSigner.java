package com.ekycrail.security;

import reactor.core.publisher.Mono;

public interface JwtSigner {
    Mono<String> sign(VerificationAssertionClaims claims);
}
