---
name: Review checklist
description: Review a PR or diff against this repo's non-negotiables before approving.
triggers: [review, PR, pull request, code review, check this, approve]
---

# Review checklist

Use this before approving or shipping any change.

## Must-pass

- [ ] **Pattern visibility.** If the PR touches a pattern's home file, the pattern is still named in the class javadoc and dispatched via polymorphism (not collapsed to `switch` / `instanceof`).
- [ ] **No cross-screen statics** introduced. Data passes through constructors.
- [ ] **ScoreManager** is still used via `INSTANCE` — no new singletons invented ad-hoc.
- [ ] **`EventBus` subscribers unsubscribe** in `hide()` / `dispose()` — no leaks across screen switches.
- [ ] **Disposables disposed.** Every `new Texture`, `new SpriteBatch`, `new Stage` has a matching `dispose()`.
- [ ] **`render()` doesn't allocate.** No `new Vector2(...)` / `new Rectangle(...)` in hot paths.
- [ ] **No secrets committed.** Scan for `pk_`, `api_`, `token`, `secret`, `BEGIN PRIVATE KEY`.
- [ ] **Gradle builds clean.** `./gradlew compileJava` passes.
- [ ] **Tests pass.** `./gradlew test`.

## Nice-to-have

- [ ] JUnit test added for the new class (one per production class is the target).
- [ ] PR title follows `[A#]` / `[I#]` prefix convention matching the ClickUp task.
- [ ] If a pattern is added, `docs/ARCHITECTURE.md` mentions it.

## Red flags (stop and ask)

- 🚩 `Platform x = new GreenPlatform(...)` outside `PlatformFactory` — Factory Method broken.
- 🚩 `if (platform instanceof GreenPlatform) …` anywhere — Strategy broken.
- 🚩 Boolean flags `isPaused`, `isGameOver` added to GameScreen — State pattern being bypassed.
- 🚩 `static int score` added somewhere — Singleton being duplicated.
- 🚩 `Gdx.input.isKeyPressed(...)` in a gameplay class — Adapter being bypassed.

## How to deliver a review

Summarize in three buckets:
1. **Blockers** — any "Must-pass" that failed.
2. **Suggestions** — nice-to-haves.
3. **Praise** — specifically for anything that *strengthens* pattern visibility (people respond well and it reinforces the behavior).
