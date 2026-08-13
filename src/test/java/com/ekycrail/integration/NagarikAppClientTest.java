package com.ekycrail.integration;

import com.ekycrail.avro.identity.AuditMetadata;
import com.ekycrail.avro.identity.CallbackMode;
import com.ekycrail.avro.identity.Channel;
import com.ekycrail.avro.identity.ContentEncryptionAlgorithm;
import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import com.ekycrail.avro.identity.KeySpec;
import com.ekycrail.avro.identity.PipelineStatus;
import com.ekycrail.avro.identity.PurposeCode;
import com.ekycrail.avro.identity.RequestContext;
import com.ekycrail.avro.identity.ResponseEncryption;
import com.ekycrail.avro.identity.VerificationRequest;
import com.ekycrail.avro.identity.VerificationScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NagarikAppClientTest {

    private DisposableServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @Test
    void shouldReturnSuccessResultForValidUpstreamResponse() {
        String responseJson = "{\"status\":\"SUCCEEDED\",\"resultCode\":\"OK\",\"signedJwt\":\"signed.jwt\",\"keyId\":\"kms-key-1\",\"expiresAt\":1735689600000,\"respondedAt\":\"2026-08-09T00:00:00Z\"}";
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.post("/verify", (req, res) ->
                        res.header("Content-Type", "application/json")
                                .sendString(Mono.just(responseJson))))
                .bindNow();

        NagarikAppClient client = new NagarikAppClient(
                WebClient.builder(),
                "http://127.0.0.1:" + server.port(),
                "/verify",
                1000,
                1000
        );

        StepVerifier.create(client.dispatchVerification(sampleEnvelope()))
                .assertNext(result -> {
                    assertTrue(!result.fallback());
                    org.junit.jupiter.api.Assertions.assertEquals("SUCCEEDED", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals("OK", result.resultCode());
                    org.junit.jupiter.api.Assertions.assertEquals("signed.jwt", result.signedJwt());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnParseFallbackWhenUpstreamPayloadIsInvalid() {
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.post("/verify", (req, res) ->
                        res.header("Content-Type", "application/json")
                                .sendString(Mono.just("{invalid-json"))))
                .bindNow();

        NagarikAppClient client = new NagarikAppClient(
                WebClient.builder(),
                "http://127.0.0.1:" + server.port(),
                "/verify",
                1000,
                1000
        );

        StepVerifier.create(client.dispatchVerification(sampleEnvelope()))
                .assertNext(result -> {
                    assertTrue(result.fallback());
                    org.junit.jupiter.api.Assertions.assertEquals("FAILED", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals("UPSTREAM_PARSE_ERROR", result.resultCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnTimeoutFallbackWhenUpstreamIsSlow() {
        String responseJson = "{\"status\":\"SUCCEEDED\",\"resultCode\":\"OK\",\"signedJwt\":\"signed.jwt\",\"keyId\":\"kms-key-1\",\"expiresAt\":1735689600000,\"respondedAt\":\"2026-08-09T00:00:00Z\"}";
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.post("/verify", (req, res) ->
                        res.header("Content-Type", "application/json")
                                .sendString(Mono.just(responseJson).delayElement(Duration.ofMillis(250)))))
                .bindNow();

        NagarikAppClient client = new NagarikAppClient(
                WebClient.builder(),
                "http://127.0.0.1:" + server.port(),
                "/verify",
                500,
                100
        );

        StepVerifier.create(client.dispatchVerification(sampleEnvelope()))
                .assertNext(result -> {
                    assertTrue(result.fallback());
                    org.junit.jupiter.api.Assertions.assertEquals("FAILED", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals("UPSTREAM_TIMEOUT", result.resultCode());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnNetworkFallbackWhenEndpointIsUnavailable() {
        NagarikAppClient client = new NagarikAppClient(
                WebClient.builder(),
                "http://127.0.0.1:1",
                "/verify",
                250,
                250
        );

        StepVerifier.create(client.dispatchVerification(sampleEnvelope()))
                .assertNext(result -> {
                    assertTrue(result.fallback());
                    org.junit.jupiter.api.Assertions.assertEquals("FAILED", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals("UPSTREAM_NETWORK_ERROR", result.resultCode());
                })
                .verifyComplete();
    }

    private IdentityVerificationEnvelope sampleEnvelope() {
        RequestContext requestContext = new RequestContext(
                "txn-001",
                "bank-xyz",
                Channel.API_GATEWAY,
                Instant.now(),
                1000,
                null,
                "nonce-123"
        );

        ResponseEncryption responseEncryption = new ResponseEncryption(
                "alias/ekyc-kms",
                KeySpec.RSA_2048,
                ContentEncryptionAlgorithm.A256GCM
        );

        VerificationRequest verificationRequest = new VerificationRequest(
                List.of(VerificationScope.IDENTITY_STATUS),
                PurposeCode.ACCOUNT_OPENING,
                "consent-ref-1",
                CallbackMode.SYNC,
                responseEncryption
        );

        AuditMetadata auditMetadata = new AuditMetadata(null, null, PipelineStatus.OK);

        return new IdentityVerificationEnvelope(
                "1.0.0",
                requestContext,
                verificationRequest,
                null,
                auditMetadata
        );
    }
}

