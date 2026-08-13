package com.ekycrail.web;

import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import com.ekycrail.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
@Tag(name = "eKYC Verification", description = "Reactive endpoint for identity verification orchestration")
public class VerificationRouter {

    @Bean
    @RouterOperation(
            path = "/api/v1/ekyc/verify",
            method = RequestMethod.POST,
            beanClass = VerificationHandler.class,
            beanMethod = "verify",
            operation = @Operation(
            operationId = "verifyIdentity",
            summary = "Verify identity via Nagarik upstream and return signed verification token",
            description = "Validates payload against compiled Avro schema before executing verification pipeline.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IdentityVerificationEnvelope.class),
                            examples = @ExampleObject(
                                    name = "ValidVerificationRequest",
                                    value = "{\n"
                                            + "  \"schemaVersion\": \"1.0.0\",\n"
                                            + "  \"requestContext\": {\n"
                                            + "    \"transactionId\": \"txn-100\",\n"
                                            + "    \"bankId\": \"bank-1\",\n"
                                            + "    \"channel\": \"API_GATEWAY\",\n"
                                            + "    \"requestTimestamp\": 1786272000000,\n"
                                            + "    \"timeoutMs\": 2000,\n"
                                            + "    \"idempotencyKey\": null,\n"
                                            + "    \"requestNonce\": \"nonce-100\"\n"
                                            + "  },\n"
                                            + "  \"verificationRequest\": {\n"
                                            + "    \"requestedScopes\": [\"IDENTITY_STATUS\"],\n"
                                            + "    \"purposeCode\": \"ACCOUNT_OPENING\",\n"
                                            + "    \"consentReference\": \"consent-100\",\n"
                                            + "    \"callbackMode\": \"SYNC\",\n"
                                            + "    \"responseEncryption\": {\n"
                                            + "      \"kmsKeyAlias\": \"alias/ekyc-kms\",\n"
                                            + "      \"keySpec\": \"RSA_2048\",\n"
                                            + "      \"contentEncryptionAlgorithm\": \"A256GCM\"\n"
                                            + "    }\n"
                                            + "  },\n"
                                            + "  \"verificationResponse\": null,\n"
                                            + "  \"auditMetadata\": {\n"
                                            + "    \"traceId\": null,\n"
                                            + "    \"latencyMs\": null,\n"
                                            + "    \"status\": \"OK\"\n"
                                            + "  }\n"
                                            + "}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Verification completed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = VerificationService.VerificationServiceResult.class),
                                    examples = @ExampleObject(
                                            name = "SuccessfulVerificationResponse",
                                            value = "{\n"
                                                    + "  \"transactionId\": \"txn-100\",\n"
                                                    + "  \"bankId\": \"bank-1\",\n"
                                                    + "  \"status\": \"SUCCEEDED\",\n"
                                                    + "  \"resultCode\": \"OK\",\n"
                                                    + "  \"signedJwt\": \"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...\",\n"
                                                    + "  \"latencyMs\": 120,\n"
                                                    + "  \"fallback\": false\n"
                                                    + "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Avro schema validation failed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = VerificationHandler.ApiError.class),
                                    examples = @ExampleObject(
                                            name = "InvalidPayloadResponse",
                                            value = "{\"code\":\"INVALID_REQUEST\",\"message\":\"Payload does not conform to IdentityVerificationEnvelope schema\"}"
                                    )
                            )
                    )
            }
    ))
    public RouterFunction<ServerResponse> verificationRoutes(VerificationHandler verificationHandler) {
        return RouterFunctions.route(
                POST("/api/v1/ekyc/verify").and(accept(MediaType.APPLICATION_JSON)),
                verificationHandler::verify
        );
    }
}

