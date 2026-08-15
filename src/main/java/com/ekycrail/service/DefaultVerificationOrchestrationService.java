package com.ekycrail.service;

import com.ekycrail.domain.ProviderResult;
import com.ekycrail.domain.RequestDeadline;
import com.ekycrail.domain.TransactionId;
import com.ekycrail.domain.VerificationAssertion;
import com.ekycrail.domain.VerificationTransaction;
import com.ekycrail.dto.TenantContext;
import com.ekycrail.dto.VerificationRequest;
import com.ekycrail.dto.VerificationResponse;
import java.util.Optional;
import reactor.core.publisher.Mono;

public final class DefaultVerificationOrchestrationService
        implements VerificationOrchestrationService {

    @Override
    public Mono<VerificationResponse> verify(
            VerificationRequest request,
            TenantContext tenantContext
    ) {
        return notImplemented();
    }

    @Override
    public Mono<VerificationResponse> getStatus(
            TransactionId transactionId,
            TenantContext tenantContext
    ) {
        return notImplemented();
    }

    private Mono<VerificationResponse> verifyNewTransaction(
            VerificationRequest request,
            TenantContext tenantContext
    ) {
        return notImplemented();
    }

    private Mono<ProviderResult> callProvider(
            VerificationTransaction transaction,
            RequestDeadline deadline
    ) {
        return notImplemented();
    }

    private Mono<Optional<VerificationAssertion>> issueAssertionIfSuccessful(
            VerificationTransaction transaction,
            ProviderResult result
    ) {
        return notImplemented();
    }

    private Mono<Void> audit(
            VerificationTransaction transaction,
            ProviderResult result
    ) {
        return notImplemented();
    }

    private VerificationResponse shapeResponse(
            VerificationTransaction transaction,
            ProviderResult result,
            Optional<VerificationAssertion> assertion
    ) {
        throw new UnsupportedOperationException("Verification orchestration is not implemented yet");
    }

    private <T> Mono<T> notImplemented() {
        return Mono.error(new UnsupportedOperationException("Verification orchestration is not implemented yet"));
    }
}
