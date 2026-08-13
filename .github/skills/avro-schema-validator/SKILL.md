---
name: avro-schema-validator
description: Generates, validates, and checks backward/forward/full compatibility for .avsc files when API payload schemas change.
---

# Avro Schema Validator

## When To Use
Use this skill when adding or changing Avro schemas in `src/main/avro/*.avsc`, especially for API boundary payloads or inter-service contracts.

## Goals
- Keep all payload contracts Avro-first and schema-driven.
- Catch malformed or invalid `.avsc` definitions early.
- Prevent breaking schema evolution by checking compatibility before merge.
- Produce actionable compatibility findings and required remediation.

## Mandatory Constraints
- Enforce Avro at every API boundary and inter-service payload definition.
- Do not introduce, persist, or log raw PII fields beyond strict contract needs.
- Keep logs sanitized to metadata only (`transactionId`, `bankId`, `latencyMs`, `status`).
- Favor safe, minimal schema evolution over disruptive contract rewrites.
- Align downstream handling with reactive, non-blocking service behavior.

## Compatibility Policy
- Backward compatibility: new readers must read data written with the previous schema.
- Forward compatibility: previous readers must read data written with the new schema.
- Full compatibility: both backward and forward compatibility must pass.
- Default gate: require full compatibility unless an explicit exception is requested and documented.

## Workflow Checklist
1. Identify changed `.avsc` files and affected message/API contracts.
2. Validate schema syntax and required Avro structure.
3. Compare new schema against baseline schema version.
4. Run backward compatibility checks and list failures.
5. Run forward compatibility checks and list failures.
6. Determine full compatibility result and release impact.
7. Propose the smallest safe fix for each incompatibility.
8. Report final decision: pass, pass-with-approved-exception, or fail.

## Expected Output
For each schema change, return:
- Changed schema file(s)
- Validation status (valid/invalid)
- Backward result with reasons
- Forward result with reasons
- Full compatibility decision
- Risk level and consumer impact
- Required fixes or approved exception details

