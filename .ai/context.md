---
name: DuddleJump project context
description: Single-file overview — team, tech stack, the 6 design patterns, canonical file layout, ClickUp coordinates. Load at the start of any session touching this project.
type: context
triggers: [DuddleJump, JumpHooper, doodle, design pattern, final project, LibGDX, libgdx]
---

# DuddleJump — Project Context

**One file, everything an AI agent needs to start being useful on this repo.** If anything here conflicts with `docs/ARCHITECTURE.md`, that file wins (it's the grading-facing doc).

---

## 1. What & why

- **What:** Doodle-Jump clone — infinite vertical platformer, player steers left/right, jumps automatically on platform contact.
- **Why this codebase:** university **Design Pattern Class** final project. Grade = quality of design-pattern application, not gameplay polish.
- **Where:** `/Users/a1/Uniprojects/DesignPatternClass/FinalProject` (macOS).

## 2. Team

| Role | Name | ClickUp ID | Email |
|---|---|---|---|
| Owner | Abror | `290416131` | abocorp4@gmail.com |
| Member | Ivan Kanevskii | `113423894` | ivan.kanevskii@narxoz.kz |

## 3. Tech stack

- **Java 17**, libGDX **1.12.x**, Gradle with wrapper
- Targets: **desktop (LWJGL3)** primary, Android optional
- Rendering: `SpriteBatch` + `OrthographicCamera` + `FitViewport` (480 × 800 virtual)
- UI: Scene2D for menus; raw SpriteBatch for gameplay
- Physics: **hand-rolled** Vector2 integration (no Box2D at MVP scope)
- Persistence: `Gdx.app.getPreferences("duddlejump")`
- Tests: JUnit 5 + `HeadlessApplicationConfiguration`

## 4. Repo layout

```
.
├── AGENTS.md / CLAUDE.md       ← agent entry points
├── README.md                   ← human-facing overview
├── docs/
│   ├── ARCHITECTURE.md         ← design-pattern deep dive + diagrams
│   └── MVP_TASKS.md            ← 20-task MVP split
├── .ai/                        ← canonical agent knowledge
│   ├── context.md (this file)
│   ├── conventions.md
│   ├── skills/ + memory/       ← with MANIFEST.md indexes
├── core/src/com/duddlejump/
│   ├── DuddleJumpGame.java / Config.java
│   ├── screens/    (MainMenu, Loading, Game, GameOver)
│   ├── entities/   (Doodle, Platform + 4 subclasses, PlatformSpawner)
│   ├── factories/  (PlatformFactory)
│   ├── managers/   (Assets, ScoreManager)
│   ├── input/      (InputController + Keyboard + Accelerometer)
│   ├── state/      (GameState + Playing/Paused/GameOver)
│   └── events/     (EventBus, ContactListener)
├── desktop/src/com/duddlejump/desktop/DesktopLauncher.java
├── android/src/com/duddlejump/android/AndroidLauncher.java
├── assets/
└── tests/
```

## 5. The 6 design patterns (GRADING WEIGHT — preserve visibility)

| Pattern | Home | One-liner |
|---|---|---|
| **Singleton** | `managers/ScoreManager.java` (enum singleton) | Shared score + Preferences-backed high score |
| **Factory Method** | `factories/PlatformFactory.create(type, x, y)` | Build platforms without `switch` leaking into GameScreen |
| **Strategy** | `Platform.onContact(Doodle)` overrides (Green/Red/Blue/White) | Each type defines its own reaction |
| **Observer** | `events/EventBus.java` + `events/ContactListener.java` | HUD / audio / achievements subscribe to contact events |
| **State** | `state/GameState.java` → Playing/Paused/GameOver | Clean transitions instead of boolean flags |
| **Adapter** | `input/InputController.java` → Keyboard + Accelerometer | Unify two different input APIs |

**If you "simplify" one of these into a switch, a lambda, or a boolean flag, you break the grade.**

## 6. MVP (20 tasks)

Split: **Ivan 9, Abror 11**. See `docs/MVP_TASKS.md`.

**First sprint (parallel, no blocking):** `[I01]` Gradle setup + `[A01]` `DuddleJumpGame`+Config.

**Tasks live at:** ClickUp list `901816969291` in space `901810382820` ("JumpHooper – Task management"), workspace `90182544099`. API base `https://api.clickup.com/api/v2`.

## 7. Authoritative docs

- `docs/ARCHITECTURE.md` — patterns deep-dive, entity interaction diagram, "Doodle lands on a platform" sequence diagram, state diagram.
- `docs/MVP_TASKS.md` — task IDs, dependency graph, DoD.
- `.ai/conventions.md` — how to write code in this repo.

## 8. What NOT to do

- ❌ Suggest ECS / Box2D / heavy rewrites at MVP scope.
- ❌ Hide design patterns inside lambdas or collapse them into `switch`.
- ❌ Use static fields to pass data between screens (except `ScoreManager.INSTANCE`).
- ❌ Commit / log / persist the ClickUp API token.
- ❌ Run destructive filesystem commands without explicit confirmation.
