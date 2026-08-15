package com.ekycrail.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class HealthHandler {

    public Mono<ServerResponse> liveness(ServerRequest request) {
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    public Mono<ServerResponse> readiness(ServerRequest request) {
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
