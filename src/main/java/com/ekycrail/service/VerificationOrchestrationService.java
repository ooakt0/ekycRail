package com.ekycrail.service;

import com.ekycrail.domain.TransactionId;
import com.ekycrail.dto.TenantContext;
import com.ekycrail.dto.VerificationRequest;
import com.ekycrail.dto.VerificationResponse;
import reactor.core.publisher.Mono;

public interface VerificationOrchestrationService {
    Mono<VerificationResponse> verify(
            VerificationRequest request,
            TenantContext tenantContext
    );

    Mono<VerificationResponse> getStatus(
            TransactionId transactionId,
            TenantContext tenantContext
    );
}
