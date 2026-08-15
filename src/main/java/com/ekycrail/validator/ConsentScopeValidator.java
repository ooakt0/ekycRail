package com.ekycrail.validator;

import com.ekycrail.domain.Consent;
import com.ekycrail.enums.Purpose;
import com.ekycrail.enums.VerificationScope;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface ConsentScopeValidator {
    Mono<Consent> requireValidConsent(
            String bankId,
            String consentReference,
            Purpose purpose,
            Set<VerificationScope> scopes
    );

    Mono<Void> validatePurpose(Consent consent, Purpose requestedPurpose);

    Mono<Void> validateScopes(Consent consent, Set<VerificationScope> requestedScopes);
}
