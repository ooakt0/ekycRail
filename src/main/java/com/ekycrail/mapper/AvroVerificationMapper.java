package com.ekycrail.mapper;

import com.ekycrail.avro.identity.IdentityVerificationEnvelope;
import com.ekycrail.dto.VerificationRequest;

public interface AvroVerificationMapper {
    VerificationRequest toDomain(IdentityVerificationEnvelope envelope);
}
