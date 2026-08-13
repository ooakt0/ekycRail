package com.ekycrail.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

class DocsRouterTest {

    private final RouterFunction<ServerResponse> routes = new DocsRouter().docsRoutes();
    private final WebTestClient client = WebTestClient.bindToRouterFunction(routes).build();

    @Test
    void shouldRedirectRootToSwaggerUi() {
        client.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/swagger-ui.html");
    }

    @Test
    void shouldRedirectSwaggerAliasToSwaggerUi() {
        client.get()
                .uri("/swagger")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/swagger-ui.html");
    }

    @Test
    void shouldRedirectDocsAliasToSwaggerUi() {
        client.get()
                .uri("/docs")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/swagger-ui.html");
    }
}

