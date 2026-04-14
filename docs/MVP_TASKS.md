# DuddleJump — MVP Tasks (Java / libGDX)

Split into **20 small, single-scope tasks** — 9 for Ivan, 11 for Abror. Each is sized to a 1–2 hour chunk so they're easy to pick up, finish, and close.

Tracked on ClickUp in the **"JumpHooper – Task management"** space, list id `901816969291`.

---

## Ivan — Flow / UI / persistence (9 tasks)

| # | Task | Pattern | Priority | ClickUp |
|---|---|---|---|---|
| I01 | Set up libGDX Gradle project | — | urgent | [86ex8aaye](https://app.clickup.com/t/86ex8aaye) |
| I02 | Wrap AssetManager in `Assets.java` | — | high | [86ex8aay6](https://app.clickup.com/t/86ex8aay6) |
| I03 | Loading screen with progress bar | — | normal | [86ex8aayb](https://app.clickup.com/t/86ex8aayb) |
| I04 | Main menu screen | — | high | [86ex8aaym](https://app.clickup.com/t/86ex8aaym) |
| I05 | Game over screen | — | high | [86ex8aaya](https://app.clickup.com/t/86ex8aaya) |
| I06 | `ScoreManager` singleton (current score) | **Singleton** | high | [86ex8aayh](https://app.clickup.com/t/86ex8aayh) |
| I07 | `ScoreManager` high-score persistence | — | normal | [86ex8aayc](https://app.clickup.com/t/86ex8aayc) |
| I08 | Keyboard input controller | **Adapter + Strategy** | high | [86ex8aayn](https://app.clickup.com/t/86ex8aayn) |
| I09 | Accelerometer input controller | **Adapter** | normal | [86ex8aayp](https://app.clickup.com/t/86ex8aayp) |

## Abror — Gameplay engine (11 tasks)

| # | Task | Pattern | Priority | ClickUp |
|---|---|---|---|---|
| A01 | `DuddleJumpGame` class + `Config` | — | urgent | [86ex8aay3](https://app.clickup.com/t/86ex8aay3) |
| A02 | Doodle sprite rendering | — | high | [86ex8aay9](https://app.clickup.com/t/86ex8aay9) |
| A03 | Doodle physics | — | high | [86ex8aay8](https://app.clickup.com/t/86ex8aay8) |
| A04 | `Platform` abstract base class | — | high | [86ex8aay7](https://app.clickup.com/t/86ex8aay7) |
| A05 | Green + Red platforms | **Strategy** | high | [86ex8aayd](https://app.clickup.com/t/86ex8aayd) |
| A06 | Blue + White platforms | **Strategy** | normal | [86ex8aayk](https://app.clickup.com/t/86ex8aayk) |
| A07 | `PlatformFactory` | **Factory Method** | high | [86ex8aayj](https://app.clickup.com/t/86ex8aayj) |
| A08 | `EventBus` + `ContactListener` | **Observer** | high | [86ex8aay5](https://app.clickup.com/t/86ex8aay5) |
| A09 | Collision detection | — | high | [86ex8aayf](https://app.clickup.com/t/86ex8aayf) |
| A10 | Platform spawning + camera scroll | — | normal | [86ex8aay4](https://app.clickup.com/t/86ex8aay4) |
| A11 | Game state machine | **State** | normal | [86ex8aayg](https://app.clickup.com/t/86ex8aayg) |

---

## Recommended order

### Sprint 1 — bootstrap (parallel, no cross-blocking)

| Ivan | Abror |
|---|---|
| **I01** Gradle project | **A01** `DuddleJumpGame` + `Config` |
| **I02** Assets wrapper | **A02** Doodle sprite rendering |

### Sprint 2 — first playable loop

| Ivan | Abror |
|---|---|
| **I04** Main menu | **A04** Platform base + **A05** Green/Red |
| **I08** Keyboard input | **A07** PlatformFactory |
| **I06** ScoreManager (current) | **A03** Doodle physics |

### Sprint 3 — full gameplay

| Ivan | Abror |
|---|---|
| **I03** Loading screen | **A08** EventBus + **A09** Collision |
| **I05** Game over screen | **A06** Blue/White platforms |
| **I07** High-score persistence | **A10** Spawning + camera |
| **I09** Accelerometer | **A11** State machine |

## Week plan — Tuesday, April 14, 2026 to Saturday, April 18, 2026

| Date | Ivan | Abror |
|---|---|---|
| Tue, Apr 14 | **I01** Gradle project, **I02** Assets wrapper | **A01** `DuddleJumpGame` + `Config`, **A02** Doodle sprite rendering |
| Wed, Apr 15 | **I04** Main menu, **I08** Keyboard input | **A03** Doodle physics, **A04** Platform base |
| Thu, Apr 16 | **I06** ScoreManager current score, **I03** Loading screen | **A05** Green + Red platforms, **A07** PlatformFactory |
| Fri, Apr 17 | **I05** Game over screen, **I07** High-score persistence | **A08** EventBus + `ContactListener`, **A09** Collision detection |
| Sat, Apr 18 | **I09** Accelerometer input | **A10** Platform spawning + camera scroll, **A06** Blue + White platforms, **A11** Game state machine |

---

## Dependency map (critical path)

```
  I01 (Gradle)
     │
     └──► A01 (Game+Config) ──► A02 (Doodle render) ──► A03 (Doodle physics)
                                                              │
  I02 (Assets) ──► I03 (Loading) ──► I04 (Menu)               │
                                       │                       │
  I08 (Keyboard) ────────────────────► │                       │
                                       ▼                       ▼
                                      [game loop starts]  A04 (Platform base)
                                                               │
                                                               ▼
                                                          A05 (Green/Red) + A07 (Factory)
                                                               │
                                                               ▼
                                                          A08 (EventBus) + A09 (Collision)
                                                               │
                                                               ▼
                                                          I06 (Score) ──► I07 (Persistence)
                                                               │
                                                               ▼
                                                          A10 (Scroll) + A06 (Blue/White)
                                                               │
                                                               ▼
                                                          A11 (State) ──► I05 (GameOver)
                                                               │
                                                               ▼
                                                          I09 (Accelerometer — Android polish)
```

---

## Definition of Done (per task)

- [ ] File(s) created/updated at the path in `docs/ARCHITECTURE.md`.
- [ ] Compiles cleanly (`./gradlew compileJava`).
- [ ] If the task names a design pattern, the pattern is clearly visible (named class, not hidden in a lambda or switch).
- [ ] Task moved to **complete** on ClickUp.

## Definition of Done (MVP)

- [ ] All 20 tasks **complete**.
- [ ] `./gradlew desktop:run` plays end-to-end: Menu → Game → GameOver → Restart.
- [ ] High score persists across app restart.
- [ ] No exceptions during a 2-minute play session.
- [ ] All 6 design patterns have a code home and a one-line javadoc pointing to `docs/ARCHITECTURE.md`.
