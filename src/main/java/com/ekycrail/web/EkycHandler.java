package com.ekycrail.web;

import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import com.ekycrail.dto.VerificationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class EkycHandler {

    public Mono<ServerResponse> verify(ServerRequest request) {
        return parseAndValidate(request).then(notImplemented());
    }

    public Mono<ServerResponse> getVerificationStatus(ServerRequest request) {
        return notImplemented();
    }

    private Mono<IdentityVerificationEnvelope> parseAndValidate(ServerRequest request) {
        return request.bodyToMono(IdentityVerificationEnvelope.class)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("request body must not be empty")));
    }

    private Mono<ServerResponse> toHttpResponse(VerificationResponse response) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }

    private Mono<ServerResponse> notImplemented() {
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
