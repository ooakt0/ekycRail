package com.ekycrail.service;

import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import com.ekycrail.domain.AuditLog;
import com.ekycrail.integration.NagarikAppClient;
import com.ekycrail.repository.AuditLogRepository;
import com.ekycrail.security.KmsJwtSigner;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class VerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationService.class);
    private static final String PROVIDER_TYPE = "NAGARIK_APP";

    private final NagarikAppClient nagarikAppClient;
    private final AuditLogRepository auditLogRepository;
    private final KmsJwtSigner kmsJwtSigner;
    private final List<String> jwtAudience;

    public VerificationService(
            NagarikAppClient nagarikAppClient,
            AuditLogRepository auditLogRepository,
            KmsJwtSigner kmsJwtSigner,
            @Value("${security.jwt.audience:ekyc-rail-client}") String jwtAudience
    ) {
        this.nagarikAppClient = nagarikAppClient;
        this.auditLogRepository = auditLogRepository;
        this.kmsJwtSigner = kmsJwtSigner;
        this.jwtAudience = List.of(jwtAudience);
    }

    public Mono<VerificationServiceResult> verify(IdentityVerificationEnvelope requestEnvelope) {
        if (requestEnvelope == null) {
            return Mono.error(new IllegalArgumentException("Verification request must not be null"));
        }

        Instant startedAt = Instant.now();
        VerificationContext context = VerificationContext.from(requestEnvelope);

        Mono<NagarikAppClient.NagarikDispatchResult> dispatch = nagarikAppClient.dispatchVerification(requestEnvelope);

        // The full request envelope is not retained beyond dispatch; only non-PII metadata is carried forward.
        return dispatch
                .flatMap(result -> persistAuditLog(context, result, startedAt)
                        .then(signIfSuccessful(context, result))
                        .defaultIfEmpty("")
                        .map(jwtToken -> toResult(context, result, startedAt, jwtToken)))
                .doOnSuccess(result -> {
                    if (result != null) {
                        LOGGER.info(
                                "verification_complete transactionId={} bankId={} latencyMs={} status={}",
                                result.transactionId(),
                                result.bankId(),
                                result.latencyMs(),
                                result.status());
                    }
                })
                .doOnError(error -> LOGGER.error(
                        "verification_failed transactionId={} bankId={} status=FAILED",
                        context.transactionId(),
                        context.bankId(),
                        error));
    }

    private Mono<Void> persistAuditLog(
            VerificationContext context,
            NagarikAppClient.NagarikDispatchResult dispatchResult,
            Instant startedAt
    ) {
        long latencyMs = Duration.between(startedAt, Instant.now()).toMillis();

        AuditLog auditLog = new AuditLog(
                context.transactionId(),
                context.bankId(),
                PROVIDER_TYPE,
                dispatchResult.status(),
                dispatchResult.resultCode(),
                latencyMs,
                Instant.now()
        );

        return auditLogRepository.save(auditLog).then();
    }

    private Mono<String> signIfSuccessful(
            VerificationContext context,
            NagarikAppClient.NagarikDispatchResult dispatchResult
    ) {
        if (!"SUCCEEDED".equalsIgnoreCase(dispatchResult.status())) {
            return Mono.empty();
        }

        KmsJwtSigner.VerificationTokenRequest tokenRequest = new KmsJwtSigner.VerificationTokenRequest(
                context.transactionId(),
                jwtAudience,
                context.transactionId(),
                context.bankId(),
                PROVIDER_TYPE,
                dispatchResult.status(),
                dispatchResult.resultCode(),
                context.latencyHintMs(),
                Instant.now()
        );

        return kmsJwtSigner.signVerificationToken(tokenRequest);
    }

    private VerificationServiceResult toResult(
            VerificationContext context,
            NagarikAppClient.NagarikDispatchResult dispatchResult,
            Instant startedAt,
            String issuedJwt
    ) {
        long latencyMs = Duration.between(startedAt, Instant.now()).toMillis();
        String normalizedJwt = issuedJwt == null || issuedJwt.isBlank() ? null : issuedJwt;
        return new VerificationServiceResult(
                context.transactionId(),
                context.bankId(),
                dispatchResult.status(),
                dispatchResult.resultCode(),
                normalizedJwt,
                latencyMs,
                dispatchResult.fallback()
        );
    }

    private record VerificationContext(
            String transactionId,
            String bankId,
            Long latencyHintMs
    ) {
        private static VerificationContext from(IdentityVerificationEnvelope envelope) {
            return new VerificationContext(
                    stringValue(envelope.getRequestContext().getTransactionId()),
                    stringValue(envelope.getRequestContext().getBankId()),
                    envelope.getAuditMetadata() == null || envelope.getAuditMetadata().getLatencyMs() == null
                            ? null
                            : envelope.getAuditMetadata().getLatencyMs().longValue()
            );
        }

        private static String stringValue(CharSequence value) {
            return value == null ? null : value.toString();
        }
    }

    public record VerificationServiceResult(
            String transactionId,
            String bankId,
            String status,
            String resultCode,
            String signedJwt,
            Long latencyMs,
            boolean fallback
    ) {
    }
}

