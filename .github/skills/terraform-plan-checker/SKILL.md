---
name: terraform-plan-checker
description: Validates Terraform for AWS KMS, ECS Fargate, and Supabase resources against security, reliability, and cost guardrails.
---

# Terraform Plan Checker

## When To Use
Use this skill when adding or changing Terraform under `terraform/`, especially AWS KMS, ECS/Fargate, IAM, networking, and Supabase resources.

## Goals
- Detect insecure or over-privileged infrastructure changes early.
- Prevent drift from KMS and runtime security best practices.
- Flag cost and blast-radius risks before apply.
- Produce merge-ready remediation guidance.

## Mandatory Constraints
- Enforce least-privilege IAM and scoped resource policies.
- Ensure KMS keys/policies support secure signing and encryption flows.
- Prevent secrets or sensitive values from appearing in plaintext outputs/state references.
- Favor private networking and secure defaults for compute and data paths.
- Keep recommendations compatible with eKYC Rail zero-PII operations.

## Workflow Checklist
1. Review changed Terraform modules, variables, and providers.
2. Validate syntax, plan intent, and resource dependency correctness.
3. Audit IAM policies for wildcard actions/resources and privilege escalation paths.
4. Audit KMS key policy, rotation settings, and usage scope.
5. Audit ECS/Fargate task, execution role, networking, and logging settings.
6. Audit Supabase/PostgreSQL exposure, credentials flow, and access boundaries.
7. Flag high-cost changes (overprovisioning, unnecessary always-on resources).
8. Output prioritized findings with exact remediation steps.

## Expected Output
For each plan review, return:
- Changed resources/modules
- Security findings with severity
- KMS/IAM compliance results
- Networking and data-exposure findings
- Cost-risk findings
- Recommended fixes and merge gate decision

