---
name: Onboarding — DuddleJump
description: Fast orientation for a fresh agent session on this repo. Gets you ready to help with any MVP task.
triggers: [new session, onboard, get started, where do I start, orient, introduction]
---

# Onboarding skill — DuddleJump

**Goal of this skill:** in under 2 minutes of reading, be ready to help on any MVP task.

## Step 1 — Read the context (30 seconds)

Open `.ai/context.md`. Key takeaways:
- Java / libGDX 1.12.x, Gradle build, desktop primary.
- Team of two — Abror owns gameplay engine, Ivan owns flow/UI/persistence.
- Grade depends on visibility of 6 design patterns: Singleton, Factory Method, Strategy, Observer, State, Adapter.

## Step 2 — Pick the right sub-area

Match the user's ask to the folder:

| User says | Go to |
|---|---|
| "physics", "gravity", "movement" | `entities/Doodle.java`, `Config.java` |
| "platform", "green/red/blue/white" | `entities/Platform.java`, 4 subclasses, `factories/PlatformFactory.java` |
| "collision", "contact", "event" | `entities/Doodle.checkContact`, `events/EventBus.java` |
| "score", "high score" | `managers/ScoreManager.java` |
| "menu", "game over", "screen" | `screens/*.java` |
| "keyboard", "input", "tilt", "accelerometer" | `input/InputController.java` + impls |
| "pause", "state", "game flow" | `state/GameState.java` + impls |

## Step 3 — Know which ClickUp task it maps to

`docs/MVP_TASKS.md` has the 20-task split with ClickUp IDs. Tasks are prefixed `[I#]` (Ivan) or `[A#]` (Abror). If the user references a prefix like "A5", go straight to that task.

## Step 4 — Check conventions before writing

Read `.ai/conventions.md` for:
- Package layout
- libGDX idioms (don't allocate in `update()`, `Array<T>` over `ArrayList`, etc.)
- Pattern visibility rules (don't collapse a Factory into `new ...()` at the call site)
- Constructor-over-static rule for cross-screen data

## Step 5 — Don't trip the landmines

From `AGENTS.md` non-negotiables:
1. Preserve the 6 patterns (visibility matters as much as correctness).
2. No statics for cross-screen state (except `ScoreManager.INSTANCE`).
3. No destructive filesystem commands without confirmation.
4. Never commit the ClickUp API token.

## Handy one-liners

```bash
./gradlew desktop:run         # launch game
./gradlew test                # unit tests
./gradlew compileJava         # fast sanity check
```
