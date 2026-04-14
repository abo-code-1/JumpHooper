# DuddleJump

A Doodle-Jump style endless jumper built with **Java + [libGDX](https://libgdx.com/)** as the final project for the **Design Pattern Class**.

The game is small and deliberately readable — every file has a single clear responsibility so the design patterns stay visible rather than buried in engine boilerplate.

---

## Team

| Member | Role | ClickUp ID |
|---|---|---|
| **Abror** | Gameplay engine (physics, platforms, collision, camera, state) | `290416131` |
| **Ivan** | Flow / UI / persistence (screens, input, score, assets) | `113423894` |

Tasks are tracked in the **"JumpHooper – Task management"** space on ClickUp (workspace `90182544099`, list `901816969291`).

---

## Tech stack

| Area | Choice |
|---|---|
| Language | **Java 17** |
| Engine | **libGDX 1.12.x** |
| Build | **Gradle** (wrapper committed) |
| Targets | **Desktop (LWJGL3)** primary; Android optional |
| Physics | Hand-rolled (gravity + velocity) — no Box2D needed at MVP scale |
| Rendering | `SpriteBatch` + `OrthographicCamera` |
| Assets | libGDX `AssetManager` |
| UI | `Scene2D` (`Stage`, `Actor`, `Table`) for menus only — gameplay uses raw `SpriteBatch` |
| Persistence | `Preferences` API (cross-platform key/value) |

---

## Gameplay

- Doodle (the player) auto-jumps on contact with platforms.
- Player controls horizontal movement — arrow keys / A-D on desktop, accelerometer tilt on Android.
- Four platform types:
  - 🟢 **Green** — normal bounce
  - 🔴 **Red** — breaks after one jump
  - 🔵 **Blue** — moves horizontally
  - ⚪ **White** — disappears after contact
- Score = +1 per platform landing; **high score** persists via `Preferences`.
- Game ends when Doodle falls below the camera → Game Over screen → restart.

---

## Quick start

```shell
# Desktop
./gradlew desktop:run

# Android
./gradlew android:installDebug android:run

# Tests
./gradlew test
```

---

## Project structure (Gradle multi-module)

```
.
├── build.gradle                  # root build config
├── settings.gradle               # module list
├── gradle/                       # wrapper
├── core/                         # platform-agnostic game code
│   └── src/com/duddlejump/
│       ├── DuddleJumpGame.java   # com.badlogic.gdx.Game subclass
│       ├── screens/
│       │   ├── MainMenuScreen.java
│       │   ├── GameScreen.java
│       │   └── GameOverScreen.java
│       ├── entities/
│       │   ├── Doodle.java
│       │   ├── Platform.java          (abstract)
│       │   ├── GreenPlatform.java
│       │   ├── RedPlatform.java
│       │   ├── BluePlatform.java
│       │   └── WhitePlatform.java
│       ├── factories/
│       │   └── PlatformFactory.java   # Factory Method
│       ├── managers/
│       │   ├── ScoreManager.java      # Singleton + Preferences
│       │   └── Assets.java            # wraps libGDX AssetManager
│       ├── input/
│       │   ├── InputController.java        (interface)
│       │   ├── KeyboardInputController.java
│       │   └── AccelerometerInputController.java
│       ├── state/
│       │   ├── GameState.java         (interface)
│       │   ├── PlayingState.java
│       │   ├── PausedState.java
│       │   └── GameOverState.java
│       └── events/
│           ├── ContactListener.java   (interface, Observer)
│           └── EventBus.java          (Observer dispatcher)
├── desktop/                      # LWJGL3 launcher
│   └── src/com/duddlejump/desktop/DesktopLauncher.java
├── android/                      # Android launcher (optional)
│   └── src/com/duddlejump/android/AndroidLauncher.java
├── assets/                       # shared asset root (images, fonts, skins)
│   ├── images/
│   ├── fonts/
│   └── sounds/
├── tests/                        # JUnit 5
│   └── src/com/duddlejump/...
└── docs/
    ├── ARCHITECTURE.md           # design patterns + module graph
    └── MVP_TASKS.md              # 10-task split between team members
```

---

## Design patterns applied

| Pattern | Where | Why |
|---|---|---|
| **Singleton** | `ScoreManager` | One score source of truth across screens; wraps `Preferences` |
| **Factory Method** | `PlatformFactory.create(type)` | Platform subtypes constructed without `switch` in `GameScreen` |
| **Strategy** | Platform `onContact(Doodle)` overrides | Each type defines its own reaction |
| **Observer** | `EventBus` + `ContactListener` | Screens, HUD, audio subscribe to contact/death events |
| **State** | `GameState` hierarchy inside `GameScreen` | Clean play / pause / game-over transitions |
| **Adapter** | `InputController` implementations | Unifies keyboard and accelerometer behind one interface |

See **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** for rationale, class diagrams, and code sketches.

---

## Documentation index

- **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — design-pattern deep dive, module map, dataflow.
- **[docs/MVP_TASKS.md](docs/MVP_TASKS.md)** — the 10-task MVP plan split 5/5 between team members.
