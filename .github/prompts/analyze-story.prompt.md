
---
name: analyze-story
description: Extract requirements and acceptance criteria into the first SDLC JSON handoff.
agent: requirements
---

Run the Requirements stage using [user-story.md](../../docs/user-story.md).

Extract evidence-based functional and non-functional requirements, measurable acceptance criteria, and unresolved questions. Ask for clarification when needed. Write `.sdlc/requirements.json` using the exact seven-key JSON contract in `.github/copilot-instructions.md`; put requirement and acceptance-criterion records in `decisions`, and unresolved questions in `blockers`. Mark approval as pending and stop until explicit human approval. Return the same JSON only. Do not create `docs/requirements.md`, implement application code, or commit changes.