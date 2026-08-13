---
name: pii-leak-detector
description: Scans R2DBC models, logs, and WebFlux responses for potential PII storage or logging violations.
---

# PII Leak Detector

## When To Use
Use this skill when adding or reviewing Spring Data R2DBC entities, repositories, logging statements, WebFlux handlers, and API response DTOs.

## Goals
- Enforce the zero persistent PII rule in storage and transit paths.
- Detect accidental PII logging in application and audit logs.
- Catch response payload leaks before merge.
- Recommend minimal, safe remediation for each violation.

## Mandatory Constraints
- Never persist NIN, full name, photo, phone number, or equivalent sensitive identifiers.
- Never log raw identity payloads; only metadata (`transactionId`, `bankId`, `latencyMs`, `status`).
- Ensure transient identity payloads are cleared after request completion.
- Keep Avro boundary contracts valid while minimizing exposed identity fields.
- Preserve reactive non-blocking patterns when introducing fixes.

## Workflow Checklist
1. Scan R2DBC entities and repositories for direct/derived PII persistence.
2. Scan logger calls (`info`, `debug`, `warn`, `error`) for payload or field leakage.
3. Inspect WebFlux handlers and response mappers for PII exposure.
4. Check exception paths and fallback handlers for unsanitized payload dumps.
5. Verify traces/metrics tags do not include PII values.
6. Classify findings by severity and exploitability.
7. Propose minimal code changes to mask, drop, or sanitize offending data.

## Expected Output
For each finding, return:
- Location (`file`, symbol, line range)
- Violation type (storage, log, response, exception, telemetry)
- Exposed field(s)
- Severity and risk rationale
- Safe remediation patch guidance
- Final pass/fail decision for zero-PII compliance

