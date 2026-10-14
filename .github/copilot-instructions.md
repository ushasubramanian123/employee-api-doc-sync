
# Project: Employee API Documentation Sync

- Use Java, Spring Boot, Maven, JUnit 5, and MockMvc.
- Keep the existing eight-stage SDLC sequence and human approval gates.
- Do not implement production code until requirements, architecture/design, and planning are approved.
- Read only relevant existing files. Never invent API behavior, test results, or repository facts; ask when requirements are ambiguous.
- Keep implementation minimal. Require explicit approval before commits, pushes, or PR creation; never merge.

## SDLC JSON handoffs

All stages except the terminal PR Agent use JSON handoffs under `.sdlc/`. Do not create Markdown stage reports for those stages. Existing product documentation, including `docs/api-docs.md`, remains Markdown.

Every non-PR agent response and saved handoff uses exactly this JSON shape, without Markdown fences or surrounding prose:

```json
{
  "stage": "<agent_stage>",
  "status": "completed",
  "summary": "<brief_result>",
  "artifacts": [],
  "decisions": [],
  "blockers": [],
  "next_agent": "<next_agent>"
}
```

Use only `completed`, `blocked`, or `failed` for `status`. `artifacts`, `decisions`, and `blockers` are arrays. Keep data compact and stage-specific; reference repository files by path, do not copy previous handoffs, and include only information needed downstream. A completed handoff with approval pending must not advance until the human explicitly approves.

The seven JSON handoffs are `.sdlc/requirements.json`, `.sdlc/architecture.json`, `.sdlc/design-review.json`, `.sdlc/implementation-plan.json`, `.sdlc/implementation.json`, `.sdlc/code-review.json`, and `.sdlc/verification.json`. Each stage validates and consumes its predecessor's JSON by path, along with any explicitly required earlier handoffs. Stop on invalid JSON, `blocked`, or `failed`; report what is needed to resume. Do not treat existing `docs/*.md` reports as handoffs.

The PR Agent is the sole exception: it consumes `.sdlc/verification.json`, creates or updates one feature-specific Markdown deliverable under `docs/`, and returns no JSON handoff. Preserve existing unrelated documentation.

Standalone API documentation sync preserves `docs/api-docs.md` and its human-review gate; report the operation using the JSON contract above rather than a Markdown stage report.
  