package com.ekycrail.repository;

import com.ekycrail.domain.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLogRepositoryTest {

    @Test
    void shouldExtendReactiveCrudRepositoryWithCorrectGenericTypes() {
        assertTrue(ReactiveCrudRepository.class.isAssignableFrom(AuditLogRepository.class));

        Type[] genericInterfaces = AuditLogRepository.class.getGenericInterfaces();
        ParameterizedType repositoryType = (ParameterizedType) genericInterfaces[0];

        assertEquals(ReactiveCrudRepository.class, repositoryType.getRawType());
        assertEquals(AuditLog.class, repositoryType.getActualTypeArguments()[0]);
        assertEquals(String.class, repositoryType.getActualTypeArguments()[1]);
    }

    @Test
    void shouldExposeDerivedQueryMethodsForBankAndStatus() throws NoSuchMethodException {
        Method byBankId = AuditLogRepository.class.getMethod("findAllByBankId", String.class);
        Method byStatus = AuditLogRepository.class.getMethod("findAllByStatus", String.class);

        assertEquals(Flux.class, byBankId.getReturnType());
        assertEquals(Flux.class, byStatus.getReturnType());
    }
}

