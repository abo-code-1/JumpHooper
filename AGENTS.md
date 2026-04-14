# AGENTS.md

Entry point for AI coding assistants working on this repo. Follows the [agents.md](https://agents.md) convention — readable by **Codex, Cursor, Aider, Amp, OpenCode, Gemini CLI, Cline**, and any other agent that honors `AGENTS.md`.

Claude Code reads `CLAUDE.md` (a thin pointer back to this file); all agents converge on the same `.ai/` knowledge base.

---

## What this project is

**DuddleJump** (a.k.a. **JumpHooper**) — a Doodle-Jump style endless jumper in **Java + libGDX 1.12.x**. Final project for a university **Design Pattern Class**. Grading weight is heavily on *which design patterns are applied* and *how cleanly*.

**Team of two:** Abror (owner) + Ivan. Tasks tracked on ClickUp.

---

## Non-negotiables

1. **Six design patterns must ship:** Singleton, Factory Method, Strategy, Observer, State, Adapter. See `.ai/context.md` §5 for each one's home.
2. **Patterns must be visible** — named classes, not hidden inside lambdas or collapsed into switch statements. A grader should be able to point at a file and name the pattern.
3. **No statics for cross-screen data.** Pass via constructor. The one exception is `ScoreManager.INSTANCE` (the Singleton itself).
4. **Never run destructive commands without confirmation** (`rm -rf`, `git reset --hard`, directory wipes). This repo has been nuked once already.
5. **Don't commit secrets.** The ClickUp API token is user-supplied per session; never write it into code, docs, or memory.

---

## Canonical knowledge layout

```
.ai/
├── context.md              ← full project context (load when starting a session)
├── conventions.md          ← coding conventions (load when writing code)
├── skills/
│   ├── MANIFEST.md         ← which skills exist + when to activate them
│   ├── onboarding.md
│   └── review-checklist.md
└── memory/
    ├── MANIFEST.md         ← which memory slices exist + their triggers
    ├── project.md
    ├── team.md
    └── clickup.md
```

Agents should load **only the slices they need** — skills and memory files carry YAML frontmatter with `triggers:` keywords to decide activation. Don't load the whole `.ai/` tree eagerly.

---

## Quick start for any agent

1. Read `.ai/context.md` — single-file overview (team, stack, 6 patterns, file paths, ClickUp coords).
2. If about to write code: also read `.ai/conventions.md`.
3. Scan `.ai/memory/MANIFEST.md`. Load a specific memory file only if the user's message matches its triggers.
4. Scan `.ai/skills/MANIFEST.md`. Load a specific skill only if the task matches.

## Build + run

```bash
./gradlew desktop:run      # desktop game
./gradlew test             # JUnit 5 suite
./gradlew compileJava      # sanity check without launching
```

## Tasks

Live on ClickUp — space `901810382820` ("JumpHooper – Task management"), list `901816969291`. See `docs/MVP_TASKS.md` for the 20-task split with assignees and ClickUp IDs.

---

## Per-agent notes

- **Claude Code** — also reads `CLAUDE.md`. Auto-memory at `~/.claude/projects/-Users-a1-Uniprojects-DesignPatternClass-FinalProject/memory/`. User-level skill at `~/.claude/skills/duddlejump/SKILL.md`.
- **Codex / GitHub Copilot** — reads this file (`AGENTS.md`) and optionally `.github/copilot-instructions.md`.
- **Cursor** — reads `AGENTS.md`; can also be given project rules via `.cursor/rules/` (not set up yet).
- **Aider** — reads `CONVENTIONS.md` if present; otherwise `AGENTS.md`.
