package com.ekycrail.repository;

import com.ekycrail.domain.AuditLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AuditLogRepository extends ReactiveCrudRepository<AuditLog, String> {

    Flux<AuditLog> findAllByBankId(String bankId);

    Flux<AuditLog> findAllByStatus(String status);
}

