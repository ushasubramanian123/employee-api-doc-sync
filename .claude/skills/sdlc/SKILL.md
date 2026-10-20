---
name: sdlc
description: Run the sequential, user-story-driven Employee API SDLC with story-scoped JSON handoffs, validation, human approval gates, and preserved rerun evidence.
---

# Employee API SDLC

Use this skill as the single user entry point for the end-to-end workflow for a supplied user-story file. Delegate each stage only with Claude Code's native `Agent` tool, setting `subagent_type` to the exact registered name shown below. Do not simulate agent execution, use Copilot agents, or perform a missing agent's work inline. If the native `Agent` tool or a requested registered agent is unavailable, stop with a clear error; never fall back to another agent or do the work directly.

## Inputs and run directory

- Require a user-story path, such as `user-stories/story3.md`. If it is missing or unreadable, ask for a path and stop.
- Derive the story ID from the story filename (`user-stories/story3.md` becomes `story3`).
- Use `.sdlc/claude/<story-id>/run-NNN/` for all handoffs. Never use the generic `.sdlc/` directory or substitute another story.

Before invoking any agent:

1. Confirm that all required agent files are available: `.claude/agents/requirements.md`, `architecture.md`, `design-review.md`, `planning.md`, `developer.md`, `code-review.md`, `verification.md`, and `pr.md`. If any are missing, stop and report them; do not create agents or substitute other agents.
2. Choose the next unused `run-NNN` directory for this story, starting at `run-001` and preserving every existing run.
3. Create the selected directory. Do not recreate or reuse an existing run directory.
4. Verify that the directory exists and is writable before invoking an agent. If creation or verification fails, stop and report the blocker.
5. Pass the exact verified directory path and supplied story path to every stage agent.

On Windows, use Claude Code's PowerShell tool to initialize the path:

```powershell
$storyRoot = Join-Path '.sdlc/claude' $storyId
[System.IO.Directory]::CreateDirectory($storyRoot) | Out-Null
$runNumber = 1
do {
	$runDir = Join-Path $storyRoot ('run-{0:D3}' -f $runNumber)
	$runNumber++
} while (Test-Path -LiteralPath $runDir)
New-Item -ItemType Directory -Path $runDir -ErrorAction Stop | Out-Null
if (-not (Test-Path -LiteralPath $runDir -PathType Container)) { throw 'Run directory verification failed.' }
$probePath = Join-Path $runDir ('.write-check-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType File -Path $probePath -ErrorAction Stop | Out-Null
Remove-Item -LiteralPath $probePath -ErrorAction Stop
```

Do not use `-Force` when creating the run directory. If the selected path is no longer unused or creation, verification, or cleanup fails, stop and report the blocker before invoking any agent; never reuse or overwrite a run directory or handoff. Remove only the uniquely named temporary check file.

Agents write only their own handoff files. Never alter prior runs or handoffs, especially `.sdlc/claude/story3/run-001/requirements.json`. Resume an earlier run only when the user explicitly requests it and every handoff required to resume validates.

## Sequential stages

Invoke one stage at a time with the native `Agent` tool. Wait for that agent to finish, validate its handoff, and satisfy any approval gate before invoking the next stage. Each stage consumes its immediate predecessor's handoff and any earlier handoffs required by that agent. Supply each agent with the story path, exact run directory, required handoff paths, and human approval decisions.

1. Requirements: invoke `Agent` with `subagent_type: requirements`; it writes `requirements.json` with `stage: requirements` and `next_agent: architecture`.
2. Architecture: invoke `Agent` with `subagent_type: architecture`; it writes `architecture.json` with `stage: architecture` and `next_agent: design-review`.
3. Design Review: invoke `Agent` with `subagent_type: design-review`; it writes `design-review.json` with `stage: design-review` and `next_agent: planning`.
4. Planning: invoke `Agent` with `subagent_type: planning`; it writes `implementation-plan.json` with `stage: planning` and `next_agent: developer`.
5. Development: invoke `Agent` with `subagent_type: developer`; it writes `implementation.json` with `stage: developer` and `next_agent: code-review`.
6. Code Review: invoke `Agent` with `subagent_type: code-review`; it writes `code-review.json` with `stage: code-review` and `next_agent: verification`.
7. Verification: invoke `Agent` with `subagent_type: verification`; it writes `verification.json` with `stage: verification` and `next_agent: pr`.
8. PR: invoke `Agent` with `subagent_type: pr`; it consumes all seven same-run handoffs (`requirements.json`, `architecture.json`, `design-review.json`, `implementation-plan.json`, `implementation.json`, `code-review.json`, and `verification.json`) and writes terminal `pr.json` with `stage: pr` and `next_agent: null`.

## Handoff validation after every stage

After each agent returns, validate its saved handoff, including after the terminal PR agent. First require the agent's final response and saved handoff to be identical JSON. Then explicitly execute the validation procedure documented in `.claude/skills/validate-handoff/SKILL.md`; that skill has `disable-model-invocation: true`, so mentioning it or expecting automatic invocation does not run validation.

From the workspace root, use Windows PowerShell 5.1 and pass the exact expected handoff path inside the current `$runDir`:

| `subagent_type` | Exact handoff path |
| --- | --- |
| `requirements` | `Join-Path $runDir 'requirements.json'` |
| `architecture` | `Join-Path $runDir 'architecture.json'` |
| `design-review` | `Join-Path $runDir 'design-review.json'` |
| `planning` | `Join-Path $runDir 'implementation-plan.json'` |
| `developer` | `Join-Path $runDir 'implementation.json'` |
| `code-review` | `Join-Path $runDir 'code-review.json'` |
| `verification` | `Join-Path $runDir 'verification.json'` |
| `pr` | `Join-Path $runDir 'pr.json'` |

For the current stage, set `$handoffPath` to its table entry and run:

```powershell
$handoffPath = Join-Path $runDir '<exact filename from the table>'
& powershell.exe -NoProfile -File ".claude/skills/validate-handoff/scripts/validate-handoff.ps1" -Path $handoffPath
$validationExitCode = $LASTEXITCODE
```

The validator must run successfully with exit code `0` and report `PASS`. If `powershell.exe` or the validator cannot be invoked, or validation returns a nonzero exit code or `FAIL`, stop immediately and report the exact handoff path and error; do not fall back to another validator, agent, or inline implementation. After `PASS`, read the handoff status: if it is `blocked`, stop and report its blockers; continue only for `completed` with every required human approval present. The validator covers all eight handoff filenames, including `pr.json`; for PR it requires `stage: pr` and JSON-null `next_agent`.

The seven-key contract is exactly `stage`, `status`, `summary`, `artifacts`, `decisions`, `blockers`, and `next_agent`. For non-PR handoffs, `stage`, `status`, `summary`, and `next_agent` are non-empty strings; arrays are required for `artifacts`, `decisions`, and `blockers`; and status may be `completed`, `blocked`, or `failed` in the agent response contract. The validator accepts `completed` or `blocked`; therefore a `failed` handoff fails validation and must stop. Check absence of blockers before continuing. Do not assume any PostToolUse hook exists or create or modify hooks as part of this workflow.

For the terminal PR stage, the same explicit procedure validates `pr.json`, and additionally require its final response to match the saved JSON exactly. Require `stage: pr`, `next_agent: null` (JSON null, not the string `"null"`), and `status` of `completed` or `blocked`, as enforced by the validator. `completed` means the requested, separately authorized operations succeeded; pending authorization must be `blocked`, never `completed`. Validate the actual operation evidence, including any commit SHA and PR URL, before accepting them as completed. Never overwrite an existing `pr.json`.

Stop on missing or invalid JSON, a mismatched response and file, incorrect stage or next agent, `blocked` or `failed` status, unresolved blockers, or pending approval. Report the handoff path and what is needed to resume. Never overwrite an existing handoff.

## Human approval and Git gates

- Require explicit human approval of Requirements before invoking Architecture.
- Require explicit human approval of the Architecture and Design Review before invoking Planning.
- Require explicit human approval of Planning before invoking Development.
- A completed handoff with approval pending is not approval. Do not infer approval from silence or from a later stage's status.
- Preserve the human-review gate for API documentation changes, including `docs/api-docs.md` when applicable.
- Require explicit authorization before each Git operation. Approval for one operation does not authorize another. Never commit, push, or create a PR without explicit authorization; never merge.
- Before staging, committing, pushing, and creating a PR, obtain separate explicit authorization for that specific action. If authorization is absent, stop before the action and ensure no unauthorized Git write is performed. Record the pending action in `pr.json` with `status: blocked`; this is not successful PR completion and does not authorize later actions.

## Reruns and PR

A rerun creates a new, verified `run-NNN` directory and preserves all previous evidence. Do not resume an earlier run unless the user explicitly asks and the relevant existing handoffs validate. If a PR handoff is blocked only because an operation-specific authorization is pending, a later explicitly requested continuation uses a new run directory: validate the seven upstream handoffs from the prior run, copy those seven handoffs unchanged into the new run directory, verify the copies, and invoke only `pr`. Do not copy or overwrite the prior `pr.json`; preserve it as the record of the pending attempt. Upstream human approvals remain evidenced by the seven validated handoffs copied unchanged, but never constitute Git authorization. Require fresh, separate explicit authorization for each remaining Git operation. Stop and report a blocker if any source handoff or copy is invalid or if the new run already contains a conflicting handoff. The PR agent consumes the continuation run's seven same-run handoffs, writes its own `pr.json`, and remains the terminal stage. Do not proceed to PR work until verification is completed and the user has explicitly authorized each Git operation as it is requested.