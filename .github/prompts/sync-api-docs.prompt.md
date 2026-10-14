---
name: sync-api-docs
description: Scan every Spring REST controller and sync verified endpoint sections in docs/api-docs.md
---

Synchronize `docs/api-docs.md` with the implemented API on every run. This prompt is started manually; do not use Git-diff detection or GitHub Actions as a trigger.

## Evidence

1. Read the approved requirements and API contract, then scan the complete application source tree for every Spring REST controller. Do not limit discovery to changed files. Resolve class-level and method-level mapping annotations, including composed mapping annotations, to determine each implemented HTTP method and path.
2. Inspect the request and response DTOs for model fields and validation constraints. Use approved requirements or contract text for endpoint descriptions. Use MockMvc assertions for response behavior, status codes, and examples. Do not infer undocumented behavior or fabricate values.
3. Mark required or optional details without supporting evidence as `Not Found` and report the missing evidence. Required details without evidence fail validation.

## Generate And Validate

1. Update only the endpoint sections in `docs/api-docs.md`. Preserve its title, introduction, and any other unrelated Markdown exactly. Add, update, or remove endpoint sections to match the complete discovered controller set; do not leave duplicate sections for a method and path.
2. Check that each discovered method and path is documented once; model details match DTOs; descriptions and examples have the stated evidence; response codes and error behavior agree with controller code and test assertions; and no sections contradict one another.
3. Render the endpoint sections a second time from the same source evidence and compare the result byte-for-byte with the first rendering. If it differs, fix nondeterministic formatting before proceeding. Validation fails on duplicates, contradictions, unsupported claims, or unresolved required evidence. Report optional unavailable details marked `Not Found`.

## Human Review Gate

After successful validation, show the generated documentation diff for `docs/api-docs.md` to the human. The diff is for review only and must never trigger discovery. Do not commit or stage changes automatically. Stop and wait for the human's explicit `APPROVED` before any commit; silence, other wording, or review of the diff is not approval. If validation fails, report findings and do not present the documentation as validated.

Return JSON only, without Markdown fences, using the seven-key handoff contract in `.github/copilot-instructions.md`. Use `stage: "api-doc-sync"` and `next_agent: "none"`. Set status to `completed`, `blocked`, or `failed` based on actual validation; reference `docs/api-docs.md` in `artifacts` and report evidence or blockers compactly. Do not create a Markdown stage report.