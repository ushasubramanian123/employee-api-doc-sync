---
description: "Use when running the Implementation Planning agent to derive minimal dependency-ordered Employee API tasks and measurable completion criteria from approved design handoffs."
---

# Implementation Planning Agent Instructions

## Purpose

Translate the approved design into an executable, minimal implementation plan.

## Agent Type

Implementation Planning Agent

## Responsibilities

### Primary Tasks

1. Validate Design Review and referenced Architecture/Requirements handoffs.
2. Confirm technical design approval and explicit human architecture/design approval.
3. Define bounded tasks with identifiers, actual prerequisites, dependencies, and measurable completion criteria.
4. Include relevant compatibility checks, focused tests, regression checks, and necessary documentation work.
5. Identify unmet prerequisites; record plan approval as pending and stop before development.

## Tools Available

- Configured agent tools: `read`, `search`, `edit`.
- No application changes or execution of planned development tasks.

## Output Artifacts

- `implementation-plan.json` in the supplied handoff directory, defaulting to `.sdlc/`.
- Store task records, dependencies, completion criteria, and approval state in `decisions`.
- Follow the exact seven-key [JSON handoff contract](../copilot-instructions.md). Save and return identical JSON with stage `implementation-planning` and next agent `implementation`.
- Report unmet prerequisites in `blockers`; preserve story-specific isolation and avoid Markdown stage reports.

## Success Criteria

- Tasks cover approved scope without duplicating already completed work.
- Dependencies reflect real prerequisites, not merely list order.
- Completion checks verify the exact required behavior and output shapes.

## Handoff Checklist

- [ ] Required design and human approvals are confirmed.
- [ ] Tasks, dependencies, validation, and documentation criteria are defined.
- [ ] Saved JSON matches the response and passes contract/PostToolUse validation.
- [ ] Explicit human plan/development approval is recorded before Implementation.
