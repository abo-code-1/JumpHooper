# Skills — Manifest

Skills are agent-agnostic reusable playbooks. Load only when the task matches a skill's triggers.

| File | Purpose | Triggers |
|---|---|---|
| [`onboarding.md`](onboarding.md) | Walk through the codebase for a fresh session | new session, onboard, get started, where do I start |
| [`review-checklist.md`](review-checklist.md) | Review a PR / diff against project rules | review, PR, pull request, code review, check this |
| [`pattern-implementation.md`](pattern-implementation.md) | Implement one of the 6 design patterns correctly | implement singleton, implement factory, implement strategy, implement observer, implement state, implement adapter |

## How to load dynamically

Every skill file starts with YAML frontmatter:

```yaml
---
name: <skill name>
description: <what it does>
triggers: [keyword1, keyword2, ...]
---
```

**Agent-specific loading:**
- **Claude Code** — this folder complements `~/.claude/skills/`. Treat in-repo skills as higher priority (they travel with the code).
- **Codex / Cursor / Aider** — these agents read `AGENTS.md` and follow the pointer here. Grep frontmatter for matching triggers before loading a file.
- **Any agent** — if unsure, default to `onboarding.md` on first interaction, then match triggers after.
