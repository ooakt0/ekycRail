package com.ekycrail.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public final class ConsentHandler {

    public Mono<ServerResponse> create(ServerRequest request) {
        return notImplemented();
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return notImplemented();
    }

    public Mono<ServerResponse> revoke(ServerRequest request) {
        return notImplemented();
    }

    public Mono<ServerResponse> list(ServerRequest request) {
        return notImplemented();
    }

    private Mono<ServerResponse> notImplemented() {
        return ServerResponse.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
