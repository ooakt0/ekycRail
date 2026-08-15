package com.ekycrail.repository;

import com.ekycrail.domain.Tenant;
import java.util.Optional;
import reactor.core.publisher.Mono;

public interface TenantRepository {
    Mono<Optional<Tenant>> findActiveByTenantId(String tenantId);

    Mono<Optional<Tenant>> findByTenantId(String tenantId);
}
