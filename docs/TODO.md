# JumpHooper — Project Finalization Todo

Live todo for the `project-finalization` branch sprint. Kept in sync with the TaskList tool; one task → one commit per conventions.

**Branch:** `project-finalization` (based on `feature/doodle`)
**Goal:** ship a very-great full build of the game in one branch, then merge to `main`.

Legend: `[x]` done · `[~]` in progress · `[ ]` queued

---

## Core MVP — 20 tasks

### Already delivered before this sprint
- [x] **I01** Set up libGDX Gradle project *(on main)*
- [x] **A01** `DuddleJumpGame` class + `Config` *(on main)*
- [x] **I02** Wrap AssetManager in `Assets.java` *(feature/bootstrap)*
- [x] **A02** Doodle sprite rendering *(feature/doodle)*

### Delivered on `project-finalization`
- [x] **I08** Keyboard input controller *(Adapter + Strategy)*
- [x] **A03** Doodle physics
- [x] **A04** `Platform` abstract base
- [x] **A05** Green + Red platforms *(Strategy)*
- [x] **A06** Blue + White platforms *(Strategy)*
- [x] **A07** `PlatformFactory` *(Factory Method)*
- [x] **A08** `EventBus` + `ContactListener` *(Observer)*

### Remaining MVP queue
- [~] **A09** Collision detection — doodle feet strip vs platform AABB
- [ ] **I06** `ScoreManager` singleton — enum singleton with current score
- [ ] **I07** High-score persistence — Gdx Preferences-backed
- [ ] **A10** Platform spawner + camera scroll — density above camera, despawn below
- [ ] **A11** Game state machine *(State)* — Playing / Paused / GameOver
- [ ] **I09** Accelerometer input controller *(Adapter)* — Android
- [ ] **I04** Main menu screen — title + Play + high score
- [ ] **I03** Loading screen — progress bar driven by Assets
- [ ] **I05** Game over screen — final + high score + restart

## "Wow" layer — after MVP
- [ ] **WOW1** `BackgroundManager` + `SkyBiome` (altitude atmosphere: earth → clouds → stratosphere → space → moon → sun → cosmic)
- [ ] **WOW2** Power-ups (jetpack / spring / shield) — Strategy subclasses
- [ ] **WOW3** Combo + screen shake + slow-mo — Observer subscribers on EventBus
- [ ] **WOW4** Dynamic music per biome + 3-layer parallax background

## Assets + polish
- [ ] Download CC0 sprites / fonts / SFX / music from Kenney, OpenGameArt, Google Fonts; log sources in `assets/CREDITS.md`
- [ ] JUnit 5 tests — `PlatformFactoryTest`, `ScoreManagerTest`, `KeyboardInputTest`, headless GameScreen smoke
- [ ] Final polish pass + README refresh (features, controls, screenshots)

---

## Conventions

1. One `[I##]` / `[A##]` per commit; `WOW#` for wow-layer, plain prefix for support tasks.
2. `./gradlew compileJava` must stay green after every commit.
3. Preserve visibility of all 6 design patterns (Singleton, Factory Method, Strategy, Observer, State, Adapter) — see `docs/ARCHITECTURE.md`.
4. No statics for cross-screen data except `ScoreManager.INSTANCE`.
5. Never merge `final/project-finalization` — it's a reference, not an input.
