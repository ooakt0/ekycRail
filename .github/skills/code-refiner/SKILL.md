---
name: code-refiner
description: Refine existing code safely while preserving architecture, security, and repository conventions.
---

# Code Refiner

Use this skill when improving or cleaning up existing code in eKYC Rail.

## Goals
- Preserve behavior unless a change is explicitly requested.
- Keep the codebase aligned with the repository persona.
- Prefer minimal, targeted edits over broad rewrites.

## Mandatory Constraints
- Follow the zero-PII rule: do not store, cache, or log identity data.
- Keep logs sanitized to metadata only.
- Use reactive, non-blocking patterns for WebFlux flows.
- Keep API and inter-service payloads Avro-aligned.
- Do not hardcode secrets, certificates, or signing keys.
- Favor secure, testable, maintainable changes.

## Refinement Checklist
1. Identify the smallest safe change.
2. Trace affected call paths and public interfaces.
3. Check for error handling, null safety, and boundary conditions.
4. Preserve or improve readability without changing semantics.
5. Add or update tests when behavior changes.
6. Validate that no PII is introduced into logs or persistence.

## Preferred Practices
- Keep methods small and single-purpose.
- Reuse existing abstractions before introducing new ones.
- Use descriptive names and consistent style.
- Prefer explicit validation and fail-fast behavior for malformed input.
- Avoid premature optimization unless profiling supports it.

## Output Expectations
When asked to refine code, provide:
- What changed
- Why it changed
- Any behavior or compatibility impact
- Follow-up work if needed


