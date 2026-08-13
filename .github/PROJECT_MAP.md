# eKYC Rail Project Map (Compact Index)

Use this file as the first stop for agent routing and context lookup.

## Update Policy (Stability)
- Update this map only for committed and merged changes that affect ownership, routing, paths, or guardrails.
- Do not update this map for draft edits, local-only work, or in-progress refactors.
- Treat this file as stable routing metadata, not a live change log.

## Guardrails (Always-On)
- Zero Persistent PII
- Metadata-only logs: `transactionId`, `bankId`, `latencyMs`, `status`
- Avro-first contracts
- KMS-backed signing/encryption
- Reactive non-blocking flows (`Mono`/`Flux`)

## Agent Quick Index
| Agent | Owns | Delegates To |
| --- | --- | --- |
| `orchestrator` | Task decomposition, routing, final integration | All peer agents/skills as needed |
| `security-auditor` | Security risk scoring, merge gate | `kms-security-audit`, `pii-leak-detector`, `avro-schema-validator` |
| `test-engineer` | Reactive test strategy and generation | `reactive-test-generator`, `avro-schema-validator` |
| `iac-architect` | Terraform architecture and review | `terraform-plan-checker`, `kms-security-audit` |

## Owner Memory Sources
- Primary merged-history memory: `.github/memory/orchestrator-merge-ledger.md`
- Stable routing metadata: `.github/PROJECT_MAP.md`
- Rule: answer merge/history questions from these files first, then validate with targeted git/PR metadata checks as needed.

## Skills Quick Index
| Skill | Use For |
| --- | --- |
| `code-refiner` | Safe refactor/readability modernization |
| `avro-schema-validator` | `.avsc` validity and compatibility gates |
| `kms-security-audit` | JWT/envelope crypto and key leakage risks |
| `pii-leak-detector` | Storage/log/response PII exposure scans |
| `reactive-test-generator` | WebFlux + StepVerifier test generation |
| `terraform-plan-checker` | IAM/KMS/network/cost Terraform review |

## Path-to-Owner Routing
| Path Prefix | Primary Owner | Secondary Checks |
| --- | --- | --- |
| `.github/agents/**` | `orchestrator` | all peers |
| `.github/skills/**` | `orchestrator` | related owning agent |
| `.github/memory/**` | `orchestrator` | none |
| `src/main/avro/**` | `security-auditor` or `test-engineer` (context-driven) | `avro-schema-validator` |
| `terraform/**` | `iac-architect` | `kms-security-audit` |

## Fast Triage Flow
1. Identify touched paths.
2. Map to owner via Path-to-Owner Routing.
3. Invoke owner and required skill delegates.
4. Require handoff verification outputs from delegate.
5. Merge into final pass/fail or pass-with-approved-exception decision.

## Merge-History Query Flow
1. Check `.github/memory/orchestrator-merge-ledger.md` first.
2. Cross-check with targeted repository metadata (commits/PR state) when needed.
3. If a change is confirmed as committed and merged, append/update ledger entry.
4. Update `PROJECT_MAP` only when merged changes affect ownership/routing/paths/guardrails.

