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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationServiceTest {

    @Test
    void shouldDispatchPersistAndSignWhenVerificationSucceeds() {
        NagarikAppClient client = mock(NagarikAppClient.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        KmsJwtSigner signer = mock(KmsJwtSigner.class);

        when(client.dispatchVerification(any())).thenReturn(Mono.just(
                new NagarikAppClient.NagarikDispatchResult("SUCCEEDED", "OK", null, null, null, false)
        ));
        when(repository.save(any(AuditLog.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(signer.signVerificationToken(any())).thenReturn(Mono.just("signed.jwt.token"));

        VerificationService service = new VerificationService(client, repository, signer, "ekyc-rail-client");

        StepVerifier.create(service.verify(sampleEnvelope()))
                .assertNext(result -> {
                    assertEquals("txn-2001", result.transactionId());
                    assertEquals("bank-22", result.bankId());
                    assertEquals("SUCCEEDED", result.status());
                    assertEquals("OK", result.resultCode());
                    assertEquals("signed.jwt.token", result.signedJwt());
                    assertEquals(false, result.fallback());
                    assertNotNull(result.latencyMs());
                })
                .verifyComplete();

        verify(client).dispatchVerification(any());
        verify(repository).save(any(AuditLog.class));
        verify(signer).signVerificationToken(any());

        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(auditCaptor.capture());
        AuditLog persisted = auditCaptor.getValue();
        assertEquals("txn-2001", persisted.getTransactionId());
        assertEquals("bank-22", persisted.getBankId());
        assertEquals("NAGARIK_APP", persisted.getProviderType());
        assertEquals("SUCCEEDED", persisted.getStatus());
        assertEquals("OK", persisted.getErrorCode());
    }

    @Test
    void shouldNotSignWhenVerificationFails() {
        NagarikAppClient client = mock(NagarikAppClient.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        KmsJwtSigner signer = mock(KmsJwtSigner.class);

        when(client.dispatchVerification(any())).thenReturn(Mono.just(
                new NagarikAppClient.NagarikDispatchResult("FAILED", "UPSTREAM_TIMEOUT", null, null, null, true)
        ));
        when(repository.save(any(AuditLog.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        VerificationService service = new VerificationService(client, repository, signer, "ekyc-rail-client");

        StepVerifier.create(service.verify(sampleEnvelope()))
                .assertNext(result -> {
                    assertEquals("FAILED", result.status());
                    assertEquals("UPSTREAM_TIMEOUT", result.resultCode());
                    assertNull(result.signedJwt());
                    assertEquals(true, result.fallback());
                })
                .verifyComplete();

        verify(repository).save(any(AuditLog.class));
        verify(signer, never()).signVerificationToken(any());
    }

    @Test
    void shouldRejectNullEnvelope() {
        NagarikAppClient client = mock(NagarikAppClient.class);
        AuditLogRepository repository = mock(AuditLogRepository.class);
        KmsJwtSigner signer = mock(KmsJwtSigner.class);

        VerificationService service = new VerificationService(client, repository, signer, "ekyc-rail-client");

        StepVerifier.create(service.verify(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    private IdentityVerificationEnvelope sampleEnvelope() {
        RequestContext requestContext = new RequestContext(
                "txn-2001",
                "bank-22",
                Channel.API_GATEWAY,
                Instant.now(),
                2000,
                null,
                "nonce-2001"
        );

        ResponseEncryption responseEncryption = new ResponseEncryption(
                "alias/ekyc-kms",
                KeySpec.RSA_2048,
                ContentEncryptionAlgorithm.A256GCM
        );

        VerificationRequest verificationRequest = new VerificationRequest(
                List.of(VerificationScope.IDENTITY_STATUS),
                PurposeCode.ACCOUNT_OPENING,
                "consent-ref-2001",
                CallbackMode.SYNC,
                responseEncryption
        );

        AuditMetadata auditMetadata = new AuditMetadata(null, 150, PipelineStatus.OK);

        return new IdentityVerificationEnvelope(
                "1.0.0",
                requestContext,
                verificationRequest,
                null,
                auditMetadata
        );
    }
}

