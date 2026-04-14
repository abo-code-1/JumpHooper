# DuddleJump — MVP Tasks (Java / libGDX)

Split **5 / 5** across Abror and Ivan. Tracked on ClickUp in the "JumpHooper – Task management" space (list id `901816969291`).

> ⚠ **Heads-up:** the ClickUp task *descriptions* still reference the old Tiny.js scaffold (src/Doodle.js, Tiny.Sprite, etc.). The file names and patterns below are the libGDX mapping — re-open and edit each ClickUp task to match, or ask Claude to sync them.

---

## Abror — Gameplay engine

| # | Task | Pattern | libGDX file(s) | Priority | ClickUp |
|---|---|---|---|---|---|
| A1 | Doodle character: physics & movement | Strategy (via `InputController`) | `core/src/com/duddlejump/entities/Doodle.java`, `Config.java` | high | [86ex8a4pc](https://app.clickup.com/t/86ex8a4pc) |
| A2 | Platform system (green / red / blue / white) | Factory Method + Strategy | `entities/Platform.java` + 4 subclasses, `factories/PlatformFactory.java` | high | [86ex8a4pu](https://app.clickup.com/t/86ex8a4pu) |
| A3 | Collision detection & contact events | Observer | `entities/Doodle.checkContact`, `events/EventBus.java`, `events/ContactListener.java` | high | [86ex8a4q5](https://app.clickup.com/t/86ex8a4q5) |
| A4 | Camera scroll & platform spawning | — | `screens/GameScreen.java` (camera logic), `entities/PlatformSpawner.java` | normal | [86ex8a4qd](https://app.clickup.com/t/86ex8a4qd) |
| A5 | Game state machine (playing / paused / game-over) | State | `state/GameState.java`, `state/PlayingState.java`, `state/PausedState.java`, `state/GameOverState.java` | normal | [86ex8a4r4](https://app.clickup.com/t/86ex8a4r4) |

## Ivan — Flow, UI, persistence

| # | Task | Pattern | libGDX file(s) | Priority | ClickUp |
|---|---|---|---|---|---|
| I1 | Main menu screen: title & Play button | — | `screens/MainMenuScreen.java` (Scene2D `Stage` + `TextButton`) | high | [86ex8a4rw](https://app.clickup.com/t/86ex8a4rw) |
| I2 | Game-over screen: score display & Restart | — | `screens/GameOverScreen.java` | high | [86ex8a4t9](https://app.clickup.com/t/86ex8a4t9) |
| I3 | Score manager with persistent high score | Singleton | `managers/ScoreManager.java` (enum singleton + `Preferences`) | high | [86ex8a4tx](https://app.clickup.com/t/86ex8a4tx) |
| I4 | Asset pipeline & loading screen | — | `managers/Assets.java` (wraps `AssetManager`), `screens/LoadingScreen.java`, `assets/` | normal | [86ex8a4v2](https://app.clickup.com/t/86ex8a4v2) |
| I5 | Input controllers: keyboard + accelerometer | Adapter | `input/InputController.java`, `input/KeyboardInputController.java`, `input/AccelerometerInputController.java` | normal | [86ex8a4v7](https://app.clickup.com/t/86ex8a4v7) |

---

## Critical path

```
(I4) Asset pipeline          (A2) Platform factory
        │                            │
        ├────────┬───────────────────┤
        │        │                   │
        ▼        ▼                   │
  (I1) Menu   (I5) Input             │
                │                    │
                ▼                    │
        (A1) Doodle physics ◄────────┘
                │
                ▼
        (A3) Collision + EventBus
                │
                ▼
        (I3) ScoreManager
                │
                ▼
        (A4) Camera scroll
                │
                ▼
        (A5) Game state machine
                │
                ▼
        (I2) Game-over screen
```

**First sprint (parallel):** I4 (Ivan) + A2 (Abror).
**Second sprint:** I5 → A1; then A3; then I3, A4, A5, I2 fan out.

---

## Status definitions (JumpHooper space)

| Status | When to use |
|---|---|
| **to do** | Default on creation |
| **planning** | Class sketch / design notes before code |
| **in progress** | Actively implementing |
| **at risk** | Blocker discovered, may slip |
| **update required** | Teammate pinged for input |
| **on hold** | Intentionally paused |
| **complete** | Merged to `main` + smoke-tested |
| **cancelled** | De-scoped for MVP |

---

## Definition of Done (MVP)

- [ ] All 10 tasks in status **complete** on ClickUp.
- [ ] `./gradlew desktop:run` launches straight into gameplay loop (Menu → Game → GameOver → Restart).
- [ ] High score persists across app restarts (`Preferences`).
- [ ] Keyboard works on desktop; accelerometer works on Android (or desktop-only is explicitly accepted).
- [ ] No exceptions during a 2-minute play session — verified in logcat / stdout.
- [ ] Each of the 6 design patterns has:
  - [ ] at least one implementing class,
  - [ ] a one-line javadoc explaining the pattern,
  - [ ] a pointer to it in `docs/ARCHITECTURE.md`.
- [ ] `./gradlew test` is green (JUnit 5 unit tests + headless smoke).
