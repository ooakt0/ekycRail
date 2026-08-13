package com.ekycrail.web;

import com.ekycrail.service.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

class VerificationHandlerTest {

    @Test
    void shouldReturnOkViaStepVerifierForValidPayload() {
        VerificationService service = mock(VerificationService.class);
        when(service.verify(any())).thenReturn(Mono.just(
                new VerificationService.VerificationServiceResult(
                        "txn-100",
                        "bank-1",
                        "SUCCEEDED",
                        "OK",
                        "signed.jwt",
                        120L,
                        false
                )
        ));

        VerificationHandler handler = new VerificationHandler(service);
        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(String.class)).thenReturn(Mono.just(validRequestJson()));

        StepVerifier.create(handler.verify(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnBadRequestViaStepVerifierForInvalidPayload() {
        VerificationService service = mock(VerificationService.class);
        VerificationHandler handler = new VerificationHandler(service);
        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(String.class)).thenReturn(Mono.just("{\"schemaVersion\":\"1.0.0\"}"));

        StepVerifier.create(handler.verify(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode());
                })
                .verifyComplete();

        verify(service, never()).verify(any());
    }

    @Test
    void shouldValidateAvroPayloadAndInvokeService() {
        VerificationService service = mock(VerificationService.class);
        when(service.verify(any())).thenReturn(Mono.just(
                new VerificationService.VerificationServiceResult(
                        "txn-100",
                        "bank-1",
                        "SUCCEEDED",
                        "OK",
                        "signed.jwt",
                        120L,
                        false
                )
        ));

        VerificationHandler handler = new VerificationHandler(service);
        RouterFunction<ServerResponse> route = RouterFunctions.route(
                POST("/api/v1/ekyc/verify"),
                handler::verify
        );

        WebTestClient client = WebTestClient.bindToRouterFunction(route).build();

        client.post()
                .uri("/api/v1/ekyc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequestJson())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.transactionId").isEqualTo("txn-100")
                .jsonPath("$.status").isEqualTo("SUCCEEDED")
                .jsonPath("$.signedJwt").isEqualTo("signed.jwt");

        verify(service).verify(any());
    }

    @Test
    void shouldRejectInvalidPayloadBeforeBusinessLogic() {
        VerificationService service = mock(VerificationService.class);
        VerificationHandler handler = new VerificationHandler(service);
        RouterFunction<ServerResponse> route = RouterFunctions.route(
                POST("/api/v1/ekyc/verify"),
                handler::verify
        );

        WebTestClient client = WebTestClient.bindToRouterFunction(route).build();

        client.post()
                .uri("/api/v1/ekyc/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"schemaVersion\":\"1.0.0\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("INVALID_REQUEST");

        verify(service, never()).verify(any());
    }

    private String validRequestJson() {
        return """
                {
                  "schemaVersion": "1.0.0",
                  "requestContext": {
                    "transactionId": "txn-100",
                    "bankId": "bank-1",
                    "channel": "API_GATEWAY",
                    "requestTimestamp": 1786272000000,
                    "timeoutMs": 2000,
                    "idempotencyKey": null,
                    "requestNonce": "nonce-100"
                  },
                  "verificationRequest": {
                    "requestedScopes": ["IDENTITY_STATUS"],
                    "purposeCode": "ACCOUNT_OPENING",
                    "consentReference": "consent-100",
                    "callbackMode": "SYNC",
                    "responseEncryption": {
                      "kmsKeyAlias": "alias/ekyc-kms",
                      "keySpec": "RSA_2048",
                      "contentEncryptionAlgorithm": "A256GCM"
                    }
                  },
                  "verificationResponse": null,
                  "auditMetadata": {
                    "traceId": null,
                    "latencyMs": null,
                    "status": "OK"
                  }
                }
                """;
    }
}

