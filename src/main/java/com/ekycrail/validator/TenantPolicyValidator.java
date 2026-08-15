package com.ekycrail.validator;

import com.ekycrail.domain.Tenant;
import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface TenantPolicyValidator {
    Mono<Tenant> requireActiveTenant(String tenantId);

    Mono<Void> validatePurpose(Tenant tenant, Purpose purpose);

    Mono<Void> validateScopes(Tenant tenant, Set<VerificationScope> scopes);
}
