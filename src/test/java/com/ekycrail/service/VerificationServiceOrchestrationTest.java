package com.ekycrail.service;

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
import com.ekycrail.domain.AuditLog;
import com.ekycrail.integration.NagarikAppClient;
import com.ekycrail.repository.AuditLogRepository;
import com.ekycrail.security.KmsJwtSigner;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationServiceOrchestrationTest {

    @Test
    void shouldOrchestrateWithMockedWebClientAndKmsClient() {
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("""
                                {
                                  "status": "SUCCEEDED",
                                  "resultCode": "OK",
                                  "signedJwt": "upstream.jwt",
                                  "keyId": "kms-key",
                                  "expiresAt": 1786272600000,
                                  "respondedAt": "2026-08-09T12:00:00Z"
                                }
                                """)
                        .build()
        ));

        WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(exchangeFunction);
        NagarikAppClient nagarikAppClient = new NagarikAppClient(
                webClientBuilder,
                "https://mock.nagarik.gov.np",
                "/verify",
                1000,
                1000
        );

        KmsClient kmsClient = mock(KmsClient.class);
        when(kmsClient.sign(any(SignRequest.class))).thenReturn(
                SignResponse.builder()
                        .keyId("arn:aws:kms:ap-south-1:123456789012:key/xyz")
                        .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
                        .signature(SdkBytes.fromByteArray(new byte[]{1, 2, 3, 4}))
                        .build()
        );

        KmsJwtSigner jwtSigner = new KmsJwtSigner(
                kmsClient,
                "arn:aws:kms:ap-south-1:123456789012:key/xyz",
                "https://issuer.ekycrail.com",
                300
        );

        AuditLogRepository repository = mock(AuditLogRepository.class);
        when(repository.save(any(AuditLog.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        VerificationService service = new VerificationService(nagarikAppClient, repository, jwtSigner, "ekyc-rail-client");

        StepVerifier.create(service.verify(sampleEnvelope()))
                .assertNext(result -> {
                    assertEquals("txn-orch-1", result.transactionId());
                    assertEquals("bank-orch", result.bankId());
                    assertEquals("SUCCEEDED", result.status());
                    assertEquals("OK", result.resultCode());
                    assertNotNull(result.signedJwt());
                })
                .verifyComplete();

        verify(exchangeFunction).exchange(any(ClientRequest.class));

        ArgumentCaptor<SignRequest> signRequestCaptor = ArgumentCaptor.forClass(SignRequest.class);
        verify(kmsClient).sign(signRequestCaptor.capture());
        SignRequest signRequest = signRequestCaptor.getValue();
        assertEquals(MessageType.RAW, signRequest.messageType());
    }

    private IdentityVerificationEnvelope sampleEnvelope() {
        RequestContext requestContext = new RequestContext(
                "txn-orch-1",
                "bank-orch",
                Channel.API_GATEWAY,
                Instant.now(),
                2000,
                null,
                "nonce-orch-1"
        );

        ResponseEncryption responseEncryption = new ResponseEncryption(
                "alias/ekyc-kms",
                KeySpec.RSA_2048,
                ContentEncryptionAlgorithm.A256GCM
        );

        VerificationRequest verificationRequest = new VerificationRequest(
                List.of(VerificationScope.IDENTITY_STATUS),
                PurposeCode.ACCOUNT_OPENING,
                "consent-orch-1",
                CallbackMode.SYNC,
                responseEncryption
        );

        AuditMetadata auditMetadata = new AuditMetadata(null, 55, PipelineStatus.OK);

        return new IdentityVerificationEnvelope(
                "1.0.0",
                requestContext,
                verificationRequest,
                null,
                auditMetadata
        );
    }
}

