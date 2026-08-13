# Orchestrator Merge Ledger

Purpose: instruction-based memory for merged changes/features so merge-history questions can be answered quickly without scanning the whole repository.

## Entry Rules
- Record only committed + merged outcomes.
- Do not record draft, local-only, or unmerged edits.
- Keep entries metadata-only and PII-safe.
- Prefer one entry per merged PR (or equivalent merge unit).

## Entry Template

```md
### YYYY-MM-DD | PR #<id> | <short title>
- Merge timestamp (UTC): <YYYY-MM-DDThh:mm:ssZ>
- Branch: <source> -> <target>
- Commit(s): <sha1>, <sha2>
- Scope: <paths or components>
- Change type: <feature|bug fix|refactor|docs|security|infra>
- User-visible outcome: <one line>
- Guardrails check: PII=<pass/fail>, Logs=<pass/fail>, Avro=<pass/fail/na>, KMS=<pass/fail/na>, Reactive=<pass/fail/na>
- Verification evidence: <tests/checks or CI run links/ids>
- Risks/notes: <residual risk or "none">
```

## Ledger Entries

<!-- Append newest entries at the top of this section. -->

