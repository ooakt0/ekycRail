package com.ekycrail.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * DocsRouter: Provides convenient routing to Swagger UI.
 * All root-level documentation requests are redirected to the Swagger UI.
 * Note: /api-docs is served directly by SpringDoc's autoconfiguration - do NOT
 * define a manual route for it here, or it will override the real OpenAPI JSON.
 */
@Configuration
public class DocsRouter {

    private static final String SWAGGER_UI_PATH = "/swagger-ui.html";

    @Bean("docsRoutes")
    public RouterFunction<ServerResponse> docsRoutes() {
        return RouterFunctions
                // Root route -> Swagger UI
                .route(GET("/"), request -> 
                    ServerResponse.temporaryRedirect(URI.create(SWAGGER_UI_PATH)).build())
                
                // /swagger -> Swagger UI
                .andRoute(GET("/swagger"), request -> 
                    ServerResponse.temporaryRedirect(URI.create(SWAGGER_UI_PATH)).build())
                
                // /docs -> Swagger UI
                .andRoute(GET("/docs"), request -> 
                    ServerResponse.temporaryRedirect(URI.create(SWAGGER_UI_PATH)).build())
                
                // Direct links to swagger-ui
                .andRoute(GET("/swagger-ui"), request -> 
                    ServerResponse.temporaryRedirect(URI.create(SWAGGER_UI_PATH)).build());
    }
}

