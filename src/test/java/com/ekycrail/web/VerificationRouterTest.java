package com.ekycrail.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationRouterTest {

    @Test
    void shouldRoutePostVerifyEndpointToHandler() {
        VerificationHandler handler = mock(VerificationHandler.class);
        when(handler.verify(any(ServerRequest.class))).thenReturn(ServerResponse.ok().build());

        VerificationRouter router = new VerificationRouter();
        RouterFunction<ServerResponse> routes = router.verificationRoutes(handler);

        WebTestClient client = WebTestClient.bindToRouterFunction(routes).build();

        client.post()
                .uri("/api/v1/ekyc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();

        verify(handler).verify(any(ServerRequest.class));
    }

    @Test
    void shouldReturnNotFoundForUnsupportedMethodOnVerifyEndpoint() {
        VerificationHandler handler = mock(VerificationHandler.class);
        when(handler.verify(any(ServerRequest.class))).thenReturn(Mono.error(new IllegalStateException("unexpected")));

        VerificationRouter router = new VerificationRouter();
        RouterFunction<ServerResponse> routes = router.verificationRoutes(handler);

        WebTestClient client = WebTestClient.bindToRouterFunction(routes).build();

        client.get()
                .uri("/api/v1/ekyc/verify")
                .exchange()
                .expectStatus().isNotFound();
    }
}


