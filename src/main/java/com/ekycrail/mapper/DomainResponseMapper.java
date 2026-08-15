package com.ekycrail.mapper;

import com.ekycrail.domain.ProviderResult;
import com.ekycrail.domain.VerificationAssertion;
import com.ekycrail.domain.VerificationTransaction;
import com.ekycrail.dto.VerificationResponse;
import java.util.Optional;

public interface DomainResponseMapper {
    VerificationResponse toResponse(
            VerificationTransaction transaction,
            ProviderResult providerResult,
            Optional<VerificationAssertion> assertion
    );
}
