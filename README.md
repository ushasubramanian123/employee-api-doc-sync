# Employee API Documentation Sync

This workspace keeps employee API requirements aligned with an approved user story.

## Workflow

1. Add or update the approved story and acceptance criteria in `docs/user-story.md`.
2. Run the `Analyze Employee API Story` prompt to derive testable requirements.
3. Review the changes and resolve open questions with the product owner.
4. Keep requirement IDs stable as the documentation evolves.

The requirements agent is defined in `.github/agents/requirements.agent.md`; project-wide guidance is in `.github/copilot-instructions.md`.

The existing PostToolUse hook validates JSON handoffs recursively under `.sdlc/`, including story-specific folders such as `.sdlc/story2/`, while preserving top-level Story 1 handoffs.