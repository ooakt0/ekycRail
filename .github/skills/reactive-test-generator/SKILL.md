---
name: reactive-test-generator
description: Generates JUnit 5, WebTestClient, and StepVerifier tests for Spring WebFlux handlers and reactive services.
---

# Reactive Test Generator

## When To Use
Use this skill when adding or changing WebFlux controllers, handlers, filters, reactive services, or gateway adapters.

## Goals
- Generate reliable reactive tests for request/response and stream behavior.
- Verify success, error, timeout, and cancellation scenarios.
- Keep tests aligned with Avro payload contracts and zero-PII rules.
- Increase confidence without introducing blocking patterns.

## Mandatory Constraints
- Use JUnit 5 with `WebTestClient` for HTTP boundaries and `StepVerifier` for reactive chains.
- Do not use blocking calls (`block`, `blockFirst`, `blockLast`) in production code assertions.
- Validate that logs and responses avoid PII leakage.
- Keep test data synthetic and non-sensitive.
- Preserve non-blocking semantics and backpressure-aware expectations.

## Workflow Checklist
1. Identify changed reactive entry points and expected contracts.
2. Generate happy-path tests with contract assertions.
3. Generate error-path tests (validation, upstream failure, crypto failure).
4. Generate timing/cancellation tests where behavior depends on reactive control flow.
5. Add assertions for sanitized logs/responses when applicable.
6. Mock dependencies (KMS/upstream/repositories) with deterministic behavior.
7. Ensure tests are isolated, readable, and maintainable.

## Expected Output
For each target component, return:
- Test files created/updated
- Covered scenarios (success/error/timeout/cancel)
- Contract assertions (status, schema fields, reactive signals)
- PII-safety assertions
- Mock strategy and assumptions
- Remaining gaps and next recommended tests

