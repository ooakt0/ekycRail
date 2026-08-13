---
name: kms-security-audit
description: Audits JWT signing and envelope encryption flows to prevent key leakage and enforce AWS KMS best practices.
---

# KMS Security Audit

## When To Use
Use this skill when implementing or reviewing cryptographic code paths, especially JWT signing/verification and envelope encryption/decryption.

## Goals
- Ensure all signing and encryption operations are KMS-backed.
- Detect key leakage risks in code, config, CI, and logs.
- Enforce safe algorithm, key usage, and IAM boundaries.
- Produce actionable remediation for every security finding.

## Mandatory Constraints
- Never hardcode private keys, data keys, certificates, or secrets.
- Use AWS SDK v2 KMS APIs for asymmetric signing and key management.
- Keep logs metadata-only (`transactionId`, `bankId`, `latencyMs`, `status`); never log plaintext, key material, or raw claims containing PII.
- Preserve zero persistent PII handling across crypto pipelines.
- Prefer fail-closed behavior on signature, key, or policy errors.

## Workflow Checklist
1. Identify all crypto entry points (JWT, envelope encryption, key retrieval).
2. Verify KMS key usage, key IDs/aliases, and algorithm compatibility (RS256/ES256 as required).
3. Confirm no secret/key material is stored in source, env dumps, artifacts, or logs.
4. Validate IAM permissions are least-privilege for `kms:Sign`, `kms:Verify`, `kms:Encrypt`, `kms:Decrypt`, and grants.
5. Check error handling for fail-closed behavior and sanitized messages.
6. Review token/crypto metadata handling for PII-safe logging.
7. Report vulnerabilities, severity, and smallest safe remediation steps.

## Expected Output
For each audited flow, return:
- Scope (files/modules/operations reviewed)
- KMS compliance status
- Key leakage findings with severity
- IAM/policy findings with severity
- Logging/PII exposure findings
- Recommended fixes and merge-blocking decisions

