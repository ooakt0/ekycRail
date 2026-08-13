package com.ekycrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * eKYC Rail: Zero-PII reactive microservice for digital identity verification.
 * 
 * Non-negotiable principles:
 * - Zero Persistent PII: Never store, cache, or log identity payloads (NIN, name, photo, phone).
 * - Avro First: All API boundaries and inter-service transfers must conform to compiled Avro schemas.
 * - KMS-Backed Crypto: Asymmetric JWT signing delegated to AWS KMS (never local key material).
 * - Reactive Patterns: Exclusively WebFlux (Mono/Flux) with non-blocking R2DBC.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ekycrail")
public class EkycRailApplication {

    public static void main(String[] args) {
        SpringApplication.run(EkycRailApplication.class, args);
    }
}

