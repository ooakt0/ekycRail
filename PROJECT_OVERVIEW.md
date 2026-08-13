# eKYC Rail: Project Overview

## Purpose

eKYC Rail is a digital-identity middleware service for Nepal's banking and financial sector. It provides a single, reactive API through which a bank can request identity verification from the Nagarik App provider and receive a standardised result.

The application is designed around a **zero-persistent-PII** model: it processes the verification request to call the provider, but its database audit trail contains transaction metadata only, not identity documents, names, phone numbers, photos, or NINs.

## What the service does

`POST /api/v1/ekyc/verify` accepts an Avro-backed JSON `IdentityVerificationEnvelope`. The request includes metadata such as the transaction and bank IDs, requested verification scopes, purpose, consent reference, and a nonce.

The request flow is:

1. The WebFlux handler parses and validates the JSON against the compiled Avro schema.
2. The service sends a reduced provider request to the configured Nagarik verification endpoint.
3. It writes a zero-PII audit record to PostgreSQL, including transaction ID, bank ID, provider, status/result code, and latency.
4. When the provider returns `SUCCEEDED`, the service creates an RS256 JWT containing verification metadata and signs it using AWS KMS.
5. It returns the transaction metadata, provider result, optional signed JWT, measured latency, and a flag showing whether a provider fallback occurred.

Upstream timeouts, transport failures, HTTP errors, malformed responses, and other client failures are normalised into non-PII failed results (for example, `UPSTREAM_TIMEOUT`) rather than exposed as provider payloads.

## API and documentation

- Verification endpoint: `POST /api/v1/ekyc/verify`
- OpenAPI JSON: `/api-docs`
- Swagger UI: `/swagger-ui.html`
- `/`, `/docs`, `/swagger`, and `/swagger-ui` redirect to Swagger UI.

The API schema is defined in `src/main/avro/identity_verification.avsc`; Maven generates the Java classes used at the HTTP boundary. The schema is the source of truth for the verification envelope.

## Technology

- Java 21 and Spring Boot 3.2
- Spring WebFlux and Reactor for non-blocking request handling
- Spring Data R2DBC with PostgreSQL for reactive persistence
- Apache Avro for the API contract
- AWS SDK v2 KMS plus Nimbus JOSE JWT for RS256 signing
- SpringDoc OpenAPI / Swagger UI
- Maven, JUnit 5, Reactor Test, and Testcontainers PostgreSQL

## Local development

The default `local` profile expects PostgreSQL at `localhost:5433`. Start it with:

```powershell
docker compose -f docker-compose.local.yml up -d
mvn spring-boot:run
```

The local Docker Compose configuration provides PostgreSQL 16 with database `ekycrail` and applies `src/main/resources/db/schema.sql` when the application starts.

Key runtime configuration is environment-driven:

- `NAGARIK_BASE_URL` and optional request-path/timeout overrides
- `AWS_REGION`, `AWS_KMS_KEY_ARN`, and `AWS_KMS_ENABLED`
- `JWT_ISSUER`, `JWT_AUDIENCE`, and `JWT_TTL_SECONDS`
- For the `dev` profile: `SUPABASE_DB_HOST`, `SUPABASE_DB_PORT`, `SUPABASE_DB_NAME`, `SUPABASE_DB_USER`, and `SUPABASE_DB_PASSWORD`

The service needs valid AWS credentials with KMS signing permission when it must issue successful verification tokens.

## Infrastructure

`terraform/` provisions an asymmetric AWS KMS key configured for `SIGN_VERIFY`, an alias, and optionally an ECS/Fargate runtime IAM role. The runtime policy is limited to signing, verification, public-key retrieval, and key description. See [terraform/README.md](terraform/README.md) for provisioning and application wiring.

## Repository layout

```text
src/main/java/com/ekycrail/
  web/          WebFlux routes, handler, and docs redirects
  service/      Verification orchestration
  integration/  Nagarik App HTTP client and fallback mapping
  security/     AWS KMS-backed JWT signer
  repository/   Reactive audit repository
  domain/       Zero-PII audit entity
  config/       KMS and OpenAPI configuration
src/main/avro/  Identity-verification contract
src/main/resources/
  db/           PostgreSQL schema
  application-*.yml  Environment profiles
src/test/       Handler, router, service, KMS, repository, and client tests
terraform/      KMS/IAM infrastructure
```

## Design guardrails

- Do not persist or log raw identity data; audit and logs are metadata-only.
- Keep API and service contracts Avro-first.
- Keep signing keys out of the application: use AWS KMS.
- Preserve reactive, non-blocking flows end to end.
- Treat provider failures as sanitised result codes and avoid leaking upstream response bodies.

## Current scope

The repository currently implements the synchronous Nagarik verification orchestration, audit logging, KMS JWT signing, local PostgreSQL development setup, OpenAPI documentation, and Terraform for the signing key/runtime role. It is a focused middleware service rather than a complete banking application or an identity-data store.

## Product direction: consent-based identity verification

The intended product direction is similar to the United States Social Security Administration's electronic Consent Based Social Security Number Verification (eCBSV) service: a government-authorised, consent-based verification API for permitted financial institutions. The service should verify whether customer-supplied identifiers, such as Nagarik number, name, and date of birth, match authoritative records. It should not become a general-purpose source of government identity records.

The key principle is **match, do not disclose**. A bank or finance company supplies a purpose-bound request and proof of customer consent. eKYC Rail then performs the approved government verification and returns a minimal signed assertion, such as match status by requested attribute, an opaque verification ID, timestamps, an approved government reference, and token expiry. It should return raw government-held attributes only where a government agreement, permitted purpose, and explicit customer consent all allow it.

```text
Permitted financial institution
  -> authenticated, consent-bound verification request
eKYC Rail
  -> authorised Nagarik App / government verification request
Government authority
  -> match decision
eKYC Rail
  -> minimal signed verification assertion
Permitted financial institution
```

Each consent should identify the financial institution, verification purpose, requested checks, customer acceptance, consent receipt ID, and creation/expiry timestamps. The platform should enforce tenant-specific access, permitted purposes, idempotency, auditability, and revocation.

Before live deployment, the project requires a formal government data-sharing agreement, technical approval, and legal/compliance review. Relevant references include the [SSA eCBSV overview](https://www.ssa.gov/dataexchange/eCBSV/), the [SSA technical guide](https://www.ssa.gov/dataexchange/eCBSV/documents/Technical%20Information%20Document%20for%20eCBSV.pdf), and Nepal's [Ministry of Home Affairs procedure for read-only citizenship-data access](https://moha.gov.np/post/na-gara-kata-pa-rama-naepata-raka-da-ta-ha-ra-na-ma-ta-ra-paha-ca-tha-na-sama). These references inform the model but do not by themselves grant permission to operate it in Nepal.
