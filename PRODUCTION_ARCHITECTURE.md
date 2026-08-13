# eKYC Rail Production Architecture

## Executive summary

eKYC Rail should be built and presented as a **consent-mediated identity-verification rail**, not as a database of citizen data. Regulated institutions integrate once with eKYC Rail; eKYC Rail verifies the minimum necessary identity attributes through an authorised Nagarik App/government integration and returns a signed verification assertion.

The core promise is: **reduce repeated government integrations without becoming a high-value store of citizens' identity data.**

## Critical prerequisite

Obtain a formal government data-sharing agreement, technical approval, and legal/compliance review before processing live data. Do not assume a public Nagarik API allows a commercial intermediary to collect, verify, or redistribute personal attributes.

Relevant sources to review with Nepali legal counsel:

- [Ministry of Home Affairs: read-only citizenship-data access framework](https://moha.gov.np/en/post/na-gara-kata-pa-rama-naepata-raka-da-ta-ha-ra-na-ma-ta-ra-paha-ca-tha-na-sama)
- [Nepal Privacy Act](https://repository.lawcommission.gov.np/np/category/documents/prevailing-law/statutes-acts/%E0%A4%B5%E0%A5%88%E0%A4%AF%E0%A4%95%E0%A5%8D%E0%A4%A4%E0%A4%BF%E0%A4%95-%E0%A4%97%E0%A5%8B%E0%A4%AA%E0%A4%A8%E0%A5%80%E0%A4%AF%E0%A4%A4%E0%A4%BE-%E0%A4%B8%E0%A4%AE%E0%A5%8D%E0%A4%AC%E0%A4%A8%E0%A5%8D/)
- [Nepal Electronic Transactions Act](https://repository.lawcommission.gov.np/np/category/documents/prevailing-law/statutes-acts/%E0%A4%B5%E0%A4%BF%E0%A4%A6%E0%A5%8D%E0%A4%AF%E0%A5%81%E0%A4%A4%E0%A5%80%E0%A4%AF-%E0%A4%87%E0%A4%B2%E0%A5%87%E0%A4%95%E0%A5%8D%E0%A4%9F%E0%A5%8D%E0%A4%B0%E0%A5%8B%E0%A4%A8%E0%A4%BF%E0%A4%95/)
- [Nepal Rastra Bank AML/CFT directives](https://www.nrb.org.np/category/aml-cft-directives/)

This document is an engineering proposal, not legal advice or a claim of regulatory compliance.

## Product flow

```text
Bank / finance company
  -> authenticated, consent-bound verification request
eKYC Rail
  -> authorised government / Nagarik App verification request
Government / Nagarik App
  -> verification decision
eKYC Rail
  -> minimal, signed verification assertion
Bank / finance company
```

The downstream request carries only the attributes required by the approved government contract, such as Nagarik number, date of birth, and name, plus a verified customer-consent record and a permitted purpose.

The standard response should be a verification assertion, not a copy of government records:

```json
{
  "verificationId": "ver_...",
  "status": "VERIFIED",
  "attributesVerified": ["name", "date_of_birth"],
  "matchLevel": "EXACT",
  "verifiedAt": "2026-08-12T00:00:00Z",
  "expiresAt": "2026-08-12T00:05:00Z",
  "evidenceReference": "gov_...",
  "signedAssertion": "..."
}
```

Return raw verified attributes only if the government agreement and customer consent explicitly allow it. Encrypt any such response to the receiving institution's public key.

## Proposed boundaries

Begin as a well-structured modular monolith rather than a collection of microservices. It is faster to secure, audit, test, and operate. Split into independent modules with explicit ownership:

| Module | Responsibility |
| --- | --- |
| Partner access | Tenant onboarding, client identity, scopes, certificates, quotas, revocation |
| Consent | Consent receipt, purpose, requested attributes, expiry, revocation |
| Verification orchestration | Request validation, workflow state, result mapping, idempotency |
| Government adapter | Nagarik-specific protocol, request signing, response mapping, reconciliation |
| Assertion service | Short-lived signed verification results and public JWKS |
| Audit and compliance | Metadata-only audit and access-event history |
| Operations | Metrics, tracing, alerts, incident controls, reconciliation tools |

Every regulated institution is a separate tenant with its own credentials, permitted purposes/scopes, rate limits, and revocation controls. A tenant must never query another tenant's verification results.

## Privacy and data lifecycle

The preferred design is **process but do not persist**.

- Hold direct identifiers only in memory for the government request.
- Do not write raw request/response bodies to application databases, logs, traces, queues, analytics, support tools, error reporting, or backups.
- Persist only tenant, opaque verification ID, purpose, consent receipt ID, status, timestamps, government reference, and a keyed/HMAC request fingerprint for idempotency.
- Use opaque IDs, never a Nagarik number, as database keys or log fields.
- Give banks a signed verification assertion; they retain their own regulated KYC evidence.
- Use short-lived assertions and support revocation where the government contract permits it.
- Build retention, deletion, consent revocation, access export, and incident response into the product.

If an asynchronous workflow absolutely requires retaining a request, use a separate short-lived PII vault with per-record KMS envelope encryption, strict TTL deletion, isolated access controls, and no raw payload in the normal application database. This is an exception, not the default.

## Security controls

### Edge and partner access

- API gateway such as AWS API Gateway, Kong, or Envoy; attach a WAF, DDoS protection, payload-size limits, and schema validation.
- OAuth 2.1 client credentials with private-key JWT client assertions, plus mutual TLS for regulated partners.
- Scope and purpose enforcement, for example `identity.verify:name-dob`, checked for every request.
- Tenant-level quotas and rate limits.

### Government integration

- Mutual TLS, explicit request signing, tight timeouts, and fixed egress IPs or private connectivity when supported.
- Government credentials and protocol knowledge live only in the government-adapter module.
- Do not expose raw government error or response bodies to partners.

### Cryptography and secrets

- TLS 1.3 in transit.
- AWS KMS/HSM-backed signing keys with rotation and separate keys per environment.
- JWS-signed verification assertions; publish a JWKS endpoint so partners can verify them.
- JWE encryption to the recipient bank's public key when a response contains permitted sensitive data.
- AWS Secrets Manager or HashiCorp Vault for secrets. Do not commit credentials, KMS identifiers, or partner secrets into source or configuration.

### Audit and engineering assurance

- Append-only metadata audit records, privileged-access audit records, and tamper-evident storage such as hash chaining or immutable object storage.
- SAST, dependency scanning, SBOM generation, secret scanning, signed builds, penetration tests, and regular access reviews.
- Threat model and data-protection impact assessment before production.

## Reliability and resilience policy

Treat the government dependency as critical and fallible.

| Concern | Policy |
| --- | --- |
| Timeout | Set explicit end-to-end, connect, and response budgets. Example: 3 seconds total, 500 ms connect, 2 seconds upstream response. |
| Idempotency | Require a downstream idempotency key; key stored outcome by tenant plus idempotency key without storing raw identity input. |
| Retry | Retry only if the government contract proves the operation is idempotent; use exponential backoff, jitter, and a small retry budget. Respect `429` and `Retry-After`. |
| Ambiguous timeout | Do not blindly resend a request that may have reached the government. Return `PENDING` or `UNKNOWN` and reconcile through an official status endpoint using a government reference. |
| Dependency outage | Use circuit breakers, bulkheads, and per-tenant rate limits. Do not silently substitute stale or fabricated verification results. |
| Slow processing | Support `202 Accepted` with signed webhook delivery and a polling endpoint. Webhooks must be signed, retried, and deduplicated. |
| Eventing | Use Kafka/SQS for metadata status/callback events only by default. Do not put raw identity payloads on shared queues. |

Use Resilience4j with Reactor Netty connection-pool tuning for the external client. Run stateless application instances across multiple availability zones and use managed, encrypted, highly available PostgreSQL.

## Technology recommendation

- Java 21, Spring Boot, WebFlux, Reactor, and R2DBC/PostgreSQL.
- Spring Security OAuth2 Resource Server and OAuth2 client support.
- Apache Avro or JSON Schema for versioned external contracts; isolate direct-identifier payload contracts from audit/result contracts.
- Resilience4j for retries, circuit breakers, bulkheads, and rate limiting.
- AWS API Gateway or Kong, AWS WAF, ECS/Fargate or EKS, RDS PostgreSQL Multi-AZ, KMS, Secrets Manager, and Terraform.
- OpenTelemetry Collector, Prometheus, Grafana, and a SIEM for observability and security operations.
- Testcontainers, WireMock, contract tests, k6/Gatling load tests, and fault-injection tests.

## Consistency and API rules

- All partner requests include `tenantId` (derived from credentials), `idempotencyKey`, `purpose`, `consentReceiptId`, and a client correlation ID.
- The platform owns verification status and audit metadata. The government owns source identity data. Each bank owns its KYC record.
- Use stable machine-readable errors such as `CONSENT_INVALID`, `TENANT_SCOPE_DENIED`, `UPSTREAM_UNAVAILABLE`, `UPSTREAM_TIMEOUT`, `VERIFICATION_PENDING`, and `VERIFICATION_FAILED`.
- Version contracts additively and publish a supported-version policy.
- Do not create a durable raw-PII queue just to make an uncertain request retryable. Instead, require government idempotency/status-query semantics and reconcile ambiguous requests.

## Current codebase gaps

The existing repository is a useful proof of concept but not yet a production identity rail:

- Its Avro schema is intentionally zero-PII; introduce a separate minimal PII ingress contract only after government approval.
- The verification endpoint does not yet enforce partner authentication, tenant authorisation, consent validation, quotas, or idempotency.
- Production configuration must require secret/environment-provided values; no identifiers or secrets should be embedded in source configuration.
- The current metadata-only audit model should become tenant-aware, append-only, access-audited, and retention controlled.
- The current client needs a complete resilience policy: bounded retry, circuit breaking, bulkheads, `PENDING` reconciliation, and observable operational alerts.

## Delivery roadmap

### Phase 0: authority and product contract

1. Secure written government authorization and API specification.
2. Agree permitted attributes, consent language, data residency, retention, government SLA/rate limits, idempotency semantics, reconciliation endpoint, and incident-notification obligations.
3. Complete a threat model, DPIA, and a bank-facing security/control pack.

### Phase 1: secure MVP

1. Add tenant onboarding, OAuth2/mTLS, scopes, rate limits, consent receipts, and idempotency.
2. Implement the government adapter, metadata-only audit trail, signed assertions, and JWKS.
3. Build contract, integration, load, and failure-path tests.

### Phase 2: operational maturity

1. Add `202` asynchronous flow, signed webhooks, reconciliation, dashboards, alerts, and on-call runbooks.
2. Complete penetration testing, disaster recovery testing, key rotation exercises, and an independent security review.
3. Add SDKs, a developer portal, sandbox environment, partner certification, and usage analytics that do not process PII.

## Investment narrative

> eKYC Rail is the secure integration layer that lets regulated institutions verify customers through government-authorised identity sources using one standard API. It reduces onboarding time and integration cost while minimising citizen-data exposure: raw identity data is processed transiently, verification decisions are signed, consent is provable, and every access is auditable.

The defensible value is not simply calling Nagarik App. It is the government partnership, policy and consent enforcement, bank-grade tenant isolation, reliable reconciliation, SDKs, and auditable compliance operations.

