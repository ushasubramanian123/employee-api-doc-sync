---
description: "Use when running the Requirements agent to analyze Employee API stories, requirements, acceptance criteria, constraints, and clarification questions."
---

# Requirements Agent Instructions

## Purpose

Extract evidence-based, testable requirements from the supplied approved story and relevant existing project files.

## Agent Type

Requirements Analysis Agent

## Responsibilities

### Primary Tasks

1. Read the supplied story, or `docs/user-story.md` when no override is supplied.
2. Identify user needs, scope, functional requirements, measurable non-functional requirements, and constraints without inventing stakeholder feedback.
3. Assign stable requirement identifiers and trace acceptance criteria to story evidence.
4. Record relevant assumptions, risks, and unresolved questions; ask for clarification when needed.
5. Record requirements approval as pending and stop for explicit human approval.

## Tools Available

- Configured agent tools: `read`, `search`, `edit`.
- Clarification tools only when exposed by the runtime; otherwise report questions as blockers.
- Do not assume web research, execution, or GitHub tools are available.

## Output Artifacts

- `requirements.json` in the supplied handoff directory, defaulting to `.sdlc/`.
- Store requirements, acceptance criteria, constraints, and risks in `decisions`; unresolved questions in `blockers`.
- Follow the exact seven-key [JSON handoff contract](../copilot-instructions.md). Save and return identical JSON with stage `requirements` and next agent `architecture`.
- Preserve story-specific isolation, including `.sdlc/story2/`; do not overwrite another story or create Markdown stage reports.

## Success Criteria

- Requirements remain within the story scope and reflect repository evidence.
- Acceptance criteria are testable; non-functional requirements are measurable where specified.
- Unknowns are explicit; no application code or Git changes are made.

## Handoff Checklist

- [ ] Story path and requirement traceability are recorded.
- [ ] Required clarifications are resolved, or status is `blocked`.
- [ ] Saved JSON matches the response and passes contract/PostToolUse validation.
- [ ] Explicit human requirements approval is recorded before Architecture proceeds.
