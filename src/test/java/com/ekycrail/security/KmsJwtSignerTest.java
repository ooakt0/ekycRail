package com.ekycrail.security;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KmsJwtSignerTest {

    @Test
    void shouldBuildOidcJwtAndSignUsingKmsRawMessageType() {
        KmsClient kmsClient = mock(KmsClient.class);
        when(kmsClient.sign(any(SignRequest.class))).thenReturn(
                SignResponse.builder()
                        .keyId("arn:aws:kms:ap-south-1:123456789012:key/abc")
                        .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
                        .signature(SdkBytes.fromByteArray(new byte[]{1, 2, 3, 4}))
                        .build()
        );

        KmsJwtSigner signer = new KmsJwtSigner(
                kmsClient,
                "arn:aws:kms:ap-south-1:123456789012:key/abc",
                "https://issuer.ekycrail.com",
                300
        );

        KmsJwtSigner.VerificationTokenRequest request = new KmsJwtSigner.VerificationTokenRequest(
                "verification-subject",
                List.of("ekyc-rail-client"),
                "txn-1001",
                "bank-11",
                "NAGARIK_APP",
                "SUCCEEDED",
                "NONE",
                145L,
                Instant.parse("2026-08-09T12:00:00Z")
        );

        StepVerifier.create(signer.signVerificationToken(request))
                .assertNext(token -> {
                    assertNotNull(token);
                    assertEquals(3, token.split("\\.").length);
                    try {
                        SignedJWT parsed = SignedJWT.parse(token);
                        assertEquals("RS256", parsed.getHeader().getAlgorithm().getName());
                        assertEquals("JWT", parsed.getHeader().getType().getType());
                        assertEquals("https://issuer.ekycrail.com", parsed.getJWTClaimsSet().getIssuer());
                        assertEquals("verification-subject", parsed.getJWTClaimsSet().getSubject());
                        assertEquals("txn-1001", parsed.getJWTClaimsSet().getStringClaim("transactionId"));
                        assertEquals("bank-11", parsed.getJWTClaimsSet().getStringClaim("bankId"));
                        assertEquals("NAGARIK_APP", parsed.getJWTClaimsSet().getStringClaim("providerType"));
                        assertEquals("SUCCEEDED", parsed.getJWTClaimsSet().getStringClaim("status"));
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                })
                .verifyComplete();

        ArgumentCaptor<SignRequest> requestCaptor = ArgumentCaptor.forClass(SignRequest.class);
        verify(kmsClient).sign(requestCaptor.capture());

        SignRequest signRequest = requestCaptor.getValue();
        assertEquals("arn:aws:kms:ap-south-1:123456789012:key/abc", signRequest.keyId());
        assertEquals(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256, signRequest.signingAlgorithm());
        assertEquals(MessageType.RAW, signRequest.messageType());
        assertTrue(signRequest.message().asByteArray().length > 0);
    }

    @Test
    void shouldRejectMissingRequiredMetadata() {
        KmsClient kmsClient = mock(KmsClient.class);
        KmsJwtSigner signer = new KmsJwtSigner(
                kmsClient,
                "arn:aws:kms:ap-south-1:123456789012:key/abc",
                "https://issuer.ekycrail.com",
                300
        );

        KmsJwtSigner.VerificationTokenRequest invalid = new KmsJwtSigner.VerificationTokenRequest(
                "verification-subject",
                List.of("ekyc-rail-client"),
                "",
                "bank-11",
                "NAGARIK_APP",
                "SUCCEEDED",
                null,
                null,
                Instant.now()
        );

        StepVerifier.create(signer.signVerificationToken(invalid))
                .expectErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException))
                .verify();
    }
}

