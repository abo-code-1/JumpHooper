---
name: Coding conventions
description: Java / libGDX conventions for this repo. Load before writing or modifying source code.
type: conventions
triggers: [write code, implement, refactor, convention, style, java]
---

# Coding Conventions

Scoped to this repo. Nothing ambitious — just the rules a grader and a teammate will notice.

## Java style

- **Java 17**, no preview features.
- 4-space indent, no tabs.
- Braces on same line (`if (...) {`).
- One public class per file; filename matches class name.
- Packages lowercase: `com.duddlejump.<area>` (`entities`, `screens`, `managers`, `input`, `state`, `events`, `factories`).

## libGDX idioms

- Extend `ScreenAdapter`, not `Screen` (fewer `@Override`s to write).
- Keep a single `SpriteBatch` per screen — don't `new SpriteBatch()` in `render()`.
- Use `Array<T>` (libGDX) instead of `ArrayList<T>` in hot paths (no autoboxing, reusable iterators).
- Use `Vector2` / `Rectangle` fields and **mutate them in place** — don't allocate in `update()`.
- Dispose every Disposable in `hide()` or `dispose()` (textures, SpriteBatch, Stage).

## Design-pattern visibility

- Name the pattern in the class Javadoc:
  ```java
  /** Singleton (enum) — shared score + high-score persistence. */
  public enum ScoreManager { ... }
  ```
- Don't collapse a Strategy into `instanceof` checks. Dispatch through the abstract method.
- Don't swap a Factory Method for inline `new GreenPlatform(...)` — the whole point is that callers stay agnostic.

## Cross-screen data

- **Constructors, not statics.** `new GameOverScreen(game, finalScore)` — not `GameOverScreen.lastScore = …`.
- One exception: `ScoreManager.INSTANCE` (the Singleton *is* the shared state mechanism).

## Tests

- JUnit 5 under `tests/src/com/duddlejump/…`.
- One test class per production class: `DoodleTest`, `PlatformFactoryTest`, …
- Mock `Preferences` with a tiny fake — don't depend on libGDX being initialized for unit tests.
- Smoke tests that need libGDX use `HeadlessApplicationConfiguration`.

## Commits & PRs

- Small PRs per ClickUp task (one `[I#]` or `[A#]` per PR).
- PR title starts with the task prefix: `[A05] Green + Red platforms`.
- PR body: what changed, which design pattern (if applicable), how it was tested.

## What good code looks like here

- A reader can skim a file header and name the design pattern.
- No `if (platform instanceof GreenPlatform)` anywhere in `GameScreen` — Strategy handles it.
- No `Gdx.input.isKeyPressed(...)` anywhere in `Doodle.java` — `InputController` handles it.
- No `ScoreManager.high = 0` from outside — expose through methods only.
