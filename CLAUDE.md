# CLAUDE.md

Claude Code entry point. **Canonical knowledge lives in [`AGENTS.md`](AGENTS.md)** and the `.ai/` directory — this file exists so Claude Code finds the right starting point.

---

## Start here

1. Read **[AGENTS.md](AGENTS.md)** — overview, non-negotiables, `.ai/` layout.
2. Read **[.ai/context.md](.ai/context.md)** — full project context.
3. Before writing code, read **[.ai/conventions.md](.ai/conventions.md)**.
4. Pull memory slices and skills on-demand from `.ai/memory/` and `.ai/skills/` (each has a `MANIFEST.md` listing triggers).

## Claude-specific extras

- **User-level skill:** `~/.claude/skills/duddlejump/SKILL.md` — loads this project's context into any Claude session, not just ones started inside the repo.
- **Auto-memory:** `~/.claude/projects/-Users-a1-Uniprojects-DesignPatternClass-FinalProject/memory/` — Claude Code writes there automatically; treat it as lower priority than `.ai/memory/` (the in-repo slice is the source of truth).
- **ClickUp API** — user supplies a personal token per session (format `pk_…`). Never persist it.

## Non-negotiables

See [AGENTS.md](AGENTS.md). Summarized: preserve all 6 design patterns, pass data via constructors, never wipe files without confirmation, don't commit secrets.
