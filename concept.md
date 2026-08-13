# eKYC Rail: Consent-Based Government Verification Gateway

## Purpose of this note

This is a concept and pilot proposal for Nagarik App and its government stakeholders. eKYC Rail enables approved banks and financial institutions to integrate once with a secure, standard verification gateway rather than each institution building and operating its own government integration.

The proposal does **not** ask for a copy of government identity records and does not position eKYC Rail as the owner of citizen data. It asks for a narrowly scoped, government-controlled verification endpoint that can answer whether permitted customer-supplied details match the authoritative record.

## The proposition in one sentence

> Nagarik App/government remains the authoritative record owner and policy authority; eKYC Rail is the approved, secure partner-access layer that returns consent-bound, minimal verification decisions to regulated institutions.

## Problem being solved

Banks and finance companies must verify customer information during account opening, lending, and regulated KYC processes. If every institution integrates directly with government systems, the government must separately manage their onboarding, credentials, certificates, security reviews, API versions, rate limits, incident handling, and support.

eKYC Rail reduces that repeated integration burden while preserving government control:

- One governed connection from eKYC Rail to the authorised government endpoint.
- A standard, auditable interface for permitted financial institutions.
- Strong customer-consent and purpose controls before any verification request is sent.
- Minimal verification outcomes rather than unrestricted registry-data disclosure.

This mirrors the principle behind the United States SSA's eCBSV programme: a consent-based match service for permitted financial entities, rather than a broad data-sharing API. The comparison is conceptual only; Nepal's legal authority, policy, and technical contract must be defined and approved by the relevant government authority.

## Product principle: match, do not disclose

The default result must be a match decision, not a citizen profile.

| Data flow | Default rule |
| --- | --- |
| Bank to eKYC Rail | Minimum customer-provided identifiers required for the approved check, plus consent and purpose. |
| eKYC Rail to government | The same minimum approved fields, sent over the government-approved secure channel. |
| Government to eKYC Rail | Match/non-match and a government reference; no unnecessary attributes. |
| eKYC Rail to bank | A short-lived, signed verification assertion with only approved check outcomes. |
| Storage | Metadata-only by default. No raw identifier, name, DOB, request, or government response body in normal databases, logs, queues, analytics, or backups. |

An illustrative result:

```json
{
  "verificationId": "ver_opaque_id",
  "status": "MATCH",
  "checks": {
    "name": "MATCH",
    "dateOfBirth": "MATCH",
    "record": "ACTIVE"
  },
  "verifiedAt": "2026-08-12T00:00:00Z",
  "expiresAt": "2026-08-12T00:05:00Z",
  "governmentReference": "gov_opaque_reference",
  "signedAssertion": "JWS"
}
```

The government decides which checks exist and which outcomes may be released. Raw government-held attributes are never returned unless an approved policy, the customer consent, and the specific use case all permit it.

## Roles and control model

| Party | Ownership and responsibility |
| --- | --- |
| Government/Nagarik App | Authoritative record, permitted data elements, match logic, service policy, endpoint access, partner approval, and revocation. |
| eKYC Rail | Secure gateway, tenant isolation, consent enforcement, standard API, signed assertions, monitoring, reconciliation, and metadata-only audit. |
| Bank/financial institution | Permitted use, customer relationship, valid consent, institution credentials, KYC decision, and its own regulated recordkeeping. |
| Customer | Grants specific, time-limited consent for the stated institution, purpose, and checks. |

The government must be able to suspend an institution or eKYC Rail immediately, reduce a quota, change allowed checks, and inspect metadata-only audit records.

## End-to-end request flow

```text
1. Customer consents at the bank for a specific purpose and requested checks.
2. Bank creates a signed request with its client credentials and idempotency key.
3. eKYC Rail authenticates the bank, enforces tenant policy, validates consent,
   validates the contract, and applies quotas.
4. eKYC Rail makes the approved mTLS/request-signed call to the government endpoint.
5. Government returns only the approved verification decision and its reference.
6. eKYC Rail records metadata-only audit information and signs the result assertion.
7. Bank receives the result synchronously, or retrieves it through an approved
   status endpoint / signed callback for asynchronous cases.
```

The only component permitted to speak the government protocol is the government-adapter module. Banks never receive government credentials and the government does not need separate custom integration logic for each bank.

## Security design

### Bank to eKYC Rail

- OAuth 2.1 client credentials using private-key JWT client assertions.
- Mutual TLS for regulated partners; certificate rotation and immediate revocation.
- API gateway with WAF, DDoS protection, request-size limits, schema validation, and per-tenant rate limits.
- Tenant-specific scopes and permitted purposes, for example `identity.verify.account-opening`.
- Mandatory idempotency key and trace/correlation ID.

### eKYC Rail to government

- Government-approved mutual TLS and request signing.
- Fixed egress IP addresses or private connectivity when made available.
- Strict TLS validation; no shared credentials with downstream institutions.
- Explicit connect, response, and end-to-end timeout budgets.
- Allow-listed endpoint, certificate, and network controls.

### Data protection

- Direct identifiers exist only in memory during the authorised verification call.
- Structured logs contain only opaque IDs, tenant ID, status, latency, and safe error codes.
- PostgreSQL audit records contain metadata only: verification ID, tenant, purpose, outcome, timestamps, reference, and latency.
- AWS KMS/HSM-backed keys sign result assertions; private keys never leave KMS.
- JWS signs result integrity; a JWKS endpoint lets banks verify signatures.
- If an approved response must carry sensitive data, encrypt it to that bank's public key using JWE.
- Store credentials in AWS Secrets Manager or HashiCorp Vault, not application configuration or source control.
- Use environment-separated accounts, keys, certificates, databases, and access roles.

### Audit and governance

- Append-only metadata audit records for verification attempts and administrative actions.
- Consent receipt ID, purpose, tenant, and result status attached to every request.
- Tamper-evident audit export or immutable retention for agreed compliance periods.
- Least-privilege access, multi-factor authentication for operators, quarterly access review, and break-glass access with full audit.
- No production data in developer machines, test environments, or support tickets.

## Reliability and failure model

The government endpoint is authoritative but can be slow, unavailable, rate-limited, or return an uncertain outcome after a network timeout. The system must fail safely rather than manufacture a result or repeatedly overload the government service.

| Situation | eKYC Rail behaviour |
| --- | --- |
| Valid immediate response | Return signed `MATCH`, `NO_MATCH`, or another government-approved result. |
| Invalid bank request or consent | Reject locally. Do not call the government endpoint. |
| Government `429` | Respect `Retry-After`, throttle the affected tenant, and return a clear retry/status result. |
| Connection failure before request dispatch | A bounded retry is permitted only within the retry budget. |
| Timeout after dispatch may have occurred | Do not blindly resend. Return `PENDING` or `UNKNOWN` and reconcile by official government transaction/status reference. |
| Government outage | Circuit breaker opens; stop new traffic to protect the government. Return a sanitised unavailable result and expose status to the bank. |
| Duplicate bank request | Return the previously stored metadata/result for the same tenant and idempotency key. |
| Slow verification | Support `202 Accepted` plus signed webhook delivery and a bank status-query endpoint. |

Use exponential backoff with jitter, a small retry budget, circuit breakers, bulkheads, and per-tenant quotas. Do not retry a request unless the government contract explicitly defines it as idempotent. Do not use stale data as a fallback verification result.

## Infrastructure proposal

The pilot should run in a dedicated, production-like cloud account/tenant with separate development, test, staging, and production environments.

```text
Bank
  -> API Gateway + WAF
  -> eKYC Rail WebFlux application (multiple stateless instances)
  -> government adapter / secure egress
  -> Government verification endpoint

eKYC Rail application
  -> managed PostgreSQL: metadata-only audit and idempotency records
  -> AWS KMS: assertion signing keys
  -> Secrets Manager: credentials and certificates
  -> OpenTelemetry / Prometheus / Grafana / SIEM: sanitised operations data
```

Recommended initial technologies:

- Java 21, Spring Boot WebFlux, Reactor Netty, and Spring Security OAuth2.
- PostgreSQL with encryption, backups, high availability, and strict network access.
- AWS ECS/Fargate or Kubernetes with multiple availability zones and auto scaling.
- AWS API Gateway or Kong/Envoy, AWS WAF, AWS KMS, Secrets Manager, CloudWatch, and Terraform.
- Resilience4j for timeout, retry, circuit-breaker, bulkhead, and rate-limit policy.
- OpenTelemetry, Prometheus, Grafana, and a SIEM for metrics, tracing, alerts, and audit operations.
- Testcontainers, WireMock, contract tests, k6/Gatling load testing, and controlled fault-injection tests.

The cloud/data-residency location, network topology, and government connectivity must be agreed with Nagarik App and the relevant authorities before production.

## API contract proposed to the government

The government contract should be small, versioned, and government controlled.

### Verification request

Required fields should include:

- Government-generated or agreed request/transaction ID.
- eKYC Rail service-provider identity and the downstream permitted institution ID.
- Consent receipt/reference, consent timestamp, and permitted purpose.
- Only the minimum approved customer-provided match fields.
- Requested checks from a government-defined list.
- Idempotency key, timestamp, nonce, and request signature.

### Verification response

The response should include:

- Government transaction/reference ID.
- Overall status: `MATCH`, `NO_MATCH`, `PENDING`, `UNAVAILABLE`, or another government-defined value.
- Per-check status only where allowed.
- Response timestamp and result expiry.
- Government signature or verifiable response integrity mechanism.
- Sanitised machine-readable error code; never raw database or citizen-record details.

### Required companion endpoints

- `GET /verification-status/{governmentReference}` for uncertain/async outcomes.
- Key/certificate distribution and rotation process.
- Sandbox endpoint with synthetic data and test certificates.
- Health/status information suitable for authorised operational use.

## What we request from Nagarik App/government

1. A formal service-provider or pilot arrangement and the responsible data controller/authority.
2. The list of permitted institutions, purposes, fields, and result types.
3. A minimal verification API and a status/reconciliation API.
4. Mutual TLS, request-signing, credential-enrollment, certificate-rotation, and network requirements.
5. Sandbox access, synthetic test data, test certificates, and certification criteria.
6. Rate limits, uptime target, maintenance process, escalation contacts, and incident-notification process.
7. Agreed data-retention, audit, deletion, data-residency, and breach-response obligations.
8. Governance for suspension, revocation, access review, and independent security assessment.

## POC scope and proof points

The POC should demonstrate control and safety, not attempt to prove access to live citizen records.

1. Use a synthetic Nagarik-compatible mock endpoint or government-provided sandbox.
2. Onboard one interested bank as a tenant with mTLS and OAuth client credentials.
3. Demonstrate consent capture/reference validation and purpose enforcement.
4. Send a schema-validated, signed request to the mock/sandbox endpoint.
5. Return a minimal signed `MATCH`/`NO_MATCH` assertion to the bank.
6. Demonstrate that direct identifiers do not appear in application logs, database audit records, traces, or error messages.
7. Demonstrate idempotency, throttling, timeout, circuit-breaker, outage, and reconciliation behaviours.
8. Demonstrate certificate/key rotation, tenant revocation, audit export, and operator alerting.

POC acceptance measures:

- No raw PII persisted or logged in the tested flow.
- Every request has a valid tenant, purpose, consent reference, and idempotency key.
- Every result can be signature-verified by the bank.
- A government outage does not create uncontrolled retries or false verification results.
- The government can audit, throttle, and revoke access.

## Why government and banks benefit

### For Nagarik App/government

- Retains full control over records, allowed checks, permitted entities, and revocation.
- Avoids supporting a different custom integration for every institution.
- Gets a controlled partner-access layer with strong consent, audit, throttling, and operational visibility.
- Receives fewer unnecessary data requests because the API is match-based and purpose-bound.

### For banks and finance companies

- One standard API, SDK, sandbox, and support model.
- Faster adoption than a direct government integration for every institution.
- Signed, auditable verification evidence for internal KYC workflows.
- Clear failure states, reconciliation, monitoring, and predictable integration behaviour.

### For citizens

- Specific consent for each institution and purpose.
- Less unnecessary copying of identity data across systems.
- Greater transparency and traceability of verification use.

## Trade-offs and decisions

| Decision | Rationale |
| --- | --- |
| Match results over raw data disclosure | Minimises exposure and makes government approval more realistic. |
| Government owns policy and records | eKYC Rail must not become a competing or shadow identity registry. |
| Modular monolith for pilot | Lowers operational complexity while retaining clear modules; split services only when scale/ownership demands it. |
| Synchronous result plus asynchronous reconciliation | Supports fast onboarding flows while safely handling government latency and uncertain timeouts. |
| Metadata-only persistence | Preserves auditability without creating a raw-PII data lake. |
| No blind retry after uncertain dispatch | Prevents duplicate government checks and false assumptions about outcome. |

## Decision record outline

**Decision:** Build eKYC Rail as a government-authorised, multi-tenant, consent-based verification gateway.

**Options considered:**

1. Each bank integrates directly with Nagarik App.
2. eKYC Rail stores and redistributes government identity data.
3. eKYC Rail performs consent-bound, minimal match verification as an approved service provider.

**Chosen direction:** Option 3, subject to government authorisation and a formal data-sharing agreement.

**Why:** It reduces integration burden while keeping the government as record owner, minimises personal-data exposure, gives banks a standard reliable interface, and creates a credible control model for approval and audit.

**Risks and follow-up:** Government may prefer to build a direct platform or may not authorise an intermediary. The pilot must therefore demonstrate that eKYC Rail reduces, rather than transfers, government security and operational burden. Final design depends on the government API contract, permitted use cases, data residency, legal review, and bank pilot requirements.
