---
name: validate-handoff
description: Validates Agentic SDLC JSON handoffs before the next Claude agent executes.
disable-model-invocation: true
---

# Validate an SDLC Handoff

Accept exactly one argument: the exact handoff file path supplied by the SDLC orchestrator. Do not infer a path, search for a handoff, modify the file, create or overwrite a run directory, or invoke another agent.

Because `disable-model-invocation: true` prevents automatic model invocation, the orchestrator must explicitly perform this validation procedure after every stage; it must not assume the skill runs automatically.

From the workspace root, run the bundled validator with the supplied path unchanged:

```powershell
powershell.exe -NoProfile -File ".claude/skills/validate-handoff/scripts/validate-handoff.ps1" -Path $handoffPath
```

Replace `$handoffPath` with the exact path passed by the stage. A `PASS` means the handoff satisfies this skill's schema and route checks. If its status is `blocked`, stop the SDLC and report its blockers even though the handoff is structurally valid. On `FAIL` or a nonzero exit code, stop and report the validation errors and path. Do not invoke the next agent unless validation passes and the status is `completed`.

The validator only reads files under `.sdlc/claude/` whose basenames are exactly `requirements.json`, `architecture.json`, `design-review.json`, `implementation-plan.json`, `implementation.json`, `code-review.json`, `verification.json`, or `pr.json`. Other paths are ignored successfully.

Targeted handoffs must be JSON objects with exactly these seven case-sensitive keys: `stage`, `status`, `summary`, `artifacts`, `decisions`, `blockers`, and `next_agent`. `stage`, `status`, and `summary` are strings; `status` is `completed` or `blocked`; `artifacts`, `decisions`, and `blockers` are arrays; `next_agent` is a string or JSON null. The required filename routes are:

| Filename | Stage | Next agent |
| --- | --- | --- |
| `requirements.json` | `requirements` | `architecture` |
| `architecture.json` | `architecture` | `design-review` |
| `design-review.json` | `design-review` | `planning` |
| `implementation-plan.json` | `planning` | `developer` |
| `implementation.json` | `developer` | `code-review` |
| `code-review.json` | `code-review` | `verification` |
| `verification.json` | `verification` | `pr` |
| `pr.json` | `pr` | JSON `null` |

`status: completed` is not human approval. Continue to apply all SDLC approval gates independently.