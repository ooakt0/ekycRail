package com.ekycrail.security;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.impl.BaseJWSProvider;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;

@Component
public class KmsJwtSigner {

    private final KmsClient kmsClient;
    private final String kmsKeyArn;
    private final String issuer;
    private final Duration tokenTtl;

    public KmsJwtSigner(
            KmsClient kmsClient,
            @Value("${aws.kms.key-arn}") String kmsKeyArn,
            @Value("${security.jwt.issuer:ekyc-rail}") String issuer,
            @Value("${security.jwt.ttl-seconds:300}") long ttlSeconds
    ) {
        this.kmsClient = kmsClient;
        this.kmsKeyArn = kmsKeyArn;
        this.issuer = issuer;
        this.tokenTtl = Duration.ofSeconds(ttlSeconds);
    }

    public Mono<String> signVerificationToken(VerificationTokenRequest request) {
        return Mono.fromCallable(() -> signInternal(request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String signInternal(VerificationTokenRequest request) throws Exception {
        validate(request);

        Instant issuedAt = request.issuedAt() == null ? Instant.now() : request.issuedAt();
        Instant expiresAt = issuedAt.plus(tokenTtl);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(request.subject())
                .audience(request.audience())
                .issueTime(java.util.Date.from(issuedAt))
                .expirationTime(java.util.Date.from(expiresAt))
                .jwtID(UUID.randomUUID().toString())
                .claim("transactionId", request.transactionId())
                .claim("bankId", request.bankId())
                .claim("providerType", request.providerType())
                .claim("status", request.status())
                .claim("errorCode", request.errorCode())
                .claim("latencyMs", request.latencyMs())
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .type(JOSEObjectType.JWT)
                        .keyID(kmsKeyArn)
                        .build(),
                claimsSet
        );

        signedJwt.sign(new KmsRs256Signer(kmsClient, kmsKeyArn));
        return signedJwt.serialize();
    }

    private void validate(VerificationTokenRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Verification token request must not be null");
        }
        if (isBlank(kmsKeyArn)) {
            throw new IllegalStateException("KMS key ARN must be configured via aws.kms.key-arn");
        }
        if (isBlank(issuer)) {
            throw new IllegalStateException("JWT issuer must be configured via security.jwt.issuer");
        }
        if (tokenTtl.isZero() || tokenTtl.isNegative()) {
            throw new IllegalStateException("JWT ttl must be positive via security.jwt.ttl-seconds");
        }
        if (isBlank(request.subject())) {
            throw new IllegalArgumentException("JWT subject must not be blank");
        }
        if (request.audience() == null || request.audience().isEmpty()) {
            throw new IllegalArgumentException("JWT audience must not be empty");
        }
        if (isBlank(request.transactionId())) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
        if (isBlank(request.bankId())) {
            throw new IllegalArgumentException("bankId must not be blank");
        }
        if (isBlank(request.providerType())) {
            throw new IllegalArgumentException("providerType must not be blank");
        }
        if (isBlank(request.status())) {
            throw new IllegalArgumentException("status must not be blank");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record VerificationTokenRequest(
            String subject,
            List<String> audience,
            String transactionId,
            String bankId,
            String providerType,
            String status,
            String errorCode,
            Long latencyMs,
            Instant issuedAt
    ) {
    }

    private static final class KmsRs256Signer extends BaseJWSProvider implements JWSSigner {

        private final KmsClient kmsClient;
        private final String keyArn;

        private KmsRs256Signer(KmsClient kmsClient, String keyArn) {
            super(Set.of(JWSAlgorithm.RS256));
            this.kmsClient = kmsClient;
            this.keyArn = keyArn;
        }

        @Override
        public Base64URL sign(JWSHeader header, byte[] signingInput) throws JOSEException {
            if (!JWSAlgorithm.RS256.equals(header.getAlgorithm())) {
                throw new JOSEException("Unsupported algorithm: " + header.getAlgorithm());
            }

            // RAW ensures KMS applies SHA-256 hashing and private-key signing internally.
            SignRequest signRequest = SignRequest.builder()
                    .keyId(keyArn)
                    .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_256)
                    .messageType(MessageType.RAW)
                    .message(SdkBytes.fromByteArray(signingInput))
                    .build();

            SignResponse signResponse = kmsClient.sign(signRequest);
            return Base64URL.encode(signResponse.signature().asByteArray());
        }

    }
}



