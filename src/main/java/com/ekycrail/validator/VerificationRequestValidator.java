package com.ekycrail.validator;

import com.ekycrail.domain.Tenant;
import com.ekycrail.dto.VerificationRequest;
import reactor.core.publisher.Mono;

public interface VerificationRequestValidator {
    Mono<Void> validateSchema(VerificationRequest request);

    Mono<Void> validateRequiredFields(VerificationRequest request);

    Mono<Void> validateTenantPolicy(VerificationRequest request, Tenant tenant);
}
