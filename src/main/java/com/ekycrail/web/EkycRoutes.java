package com.ekycrail.web;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

public final class EkycRoutes {

    public RouterFunction<ServerResponse> routes(EkycHandler handler) {
        return RouterFunctions.route(
                        POST("/api/v1/ekyc/verify").and(accept(MediaType.APPLICATION_JSON)),
                        handler::verify
                )
                .andRoute(GET("/api/v1/ekyc/verify/{transactionId}"), handler::getVerificationStatus);
    }
}
