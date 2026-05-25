# Star Jump — libGDX port

A 1:1 Java/[libGDX](https://libgdx.com/) re-implementation of **Star Jump**, the
React-Native (Expo + react-native-game-engine) doodle-jump style game in
[`../starjump`](../starjump). Same gameplay, same art, same screens — rebuilt
around classic **GoF design patterns** for the Design Patterns course.

The original was an *entity–component–system* driven by `react-native-game-engine`
(`systems.js` mutated a flat bag of entities every tick). This port keeps the
exact physics and feel but expresses the architecture with six design patterns.

---

## The 6 design patterns

| Pattern | Where | What it does |
|---|---|---|
| **Singleton** | [`ScoreManager`](core/src/com/starbots/starjump/ScoreManager.java) | The one piece of global state. Holds the live run score/jumps and the persisted record / total distance / total jumps (libGDX `Preferences`, replacing the original `AsyncStorage`). |
| **Factory Method** | [`PlatformFactory`](core/src/com/starbots/starjump/model/platform/PlatformFactory.java) → [`PlainPlatformFactory`](core/src/com/starbots/starjump/model/platform/PlainPlatformFactory.java), [`EffectPlatformFactory`](core/src/com/starbots/starjump/model/platform/EffectPlatformFactory.java) | `spawn()` is the template; the abstract `create()` factory method is overridden by two creators — plain platforms for the opening layout, effect platforms (moving / lava rolls) when recycling. |
| **Strategy** | [`PlatformBehavior`](core/src/com/starbots/starjump/model/platform/PlatformBehavior.java) → [`StaticBehavior`](core/src/com/starbots/starjump/model/platform/StaticBehavior.java), [`MovingBehavior`](core/src/com/starbots/starjump/model/platform/MovingBehavior.java), [`LavaBehavior`](core/src/com/starbots/starjump/model/platform/LavaBehavior.java) | Each platform delegates its per-frame movement and its "what happens on landing" to a swappable behaviour — the original's `effects` field (`null` / `'moving'` / `'lava'`) made polymorphic. |
| **Observer** | [`EventBus`](core/src/com/starbots/starjump/patterns/observer/EventBus.java) + [`GameEventListener`](core/src/com/starbots/starjump/patterns/observer/GameEventListener.java) → [`SoundController`](core/src/com/starbots/starjump/audio/SoundController.java), [`AchievementManager`](core/src/com/starbots/starjump/achievements/AchievementManager.java) | Jumps / score-changes / deaths are published on a bus; the sound player and the achievement unlocker subscribe. The game logic never references audio or achievements directly. |
| **State** | [`GameState`](core/src/com/starbots/starjump/patterns/state/GameState.java) + [`GameStateManager`](core/src/com/starbots/starjump/patterns/state/GameStateManager.java) → the [`screens/`](core/src/com/starbots/starjump/screens) | The original's big `render()` switch (loading / menu / game / game-over / achievements / prizes) becomes one State per screen, driven by a state-machine context. |
| **Adapter** | [`TiltControl`](core/src/com/starbots/starjump/input/TiltControl.java) → [`KeyboardTiltAdapter`](core/src/com/starbots/starjump/input/KeyboardTiltAdapter.java) (desktop), [`AccelerometerTiltAdapter`](core/src/com/starbots/starjump/input/AccelerometerTiltAdapter.java) (android) | Adapts each platform's input API to the single `getTilt()` interface the shared world expects, with the original `accelerometer.x` sign semantics. Each launcher injects the right adapter. |

> The simulation in [`World`](core/src/com/starbots/starjump/model/World.java) is a
> faithful port of `systems.js` (`gameLoop`, `jump`, `up`, `animateEffects`,
> `reset`) and pulls all six patterns together.

---

## Gameplay parity with the original

Ported verbatim from `systems.js` / `App.js` (see [`Config`](core/src/com/starbots/starjump/Config.java)):

- 9 recycling platforms, `GRAVITY = 0.25`, jump impulse `-13.6 - score/6000`.
- World scrolls only while the player is in the top third and rising; score
  accrues as `-speed/5` per scroll frame, rounded.
- Platforms recycle bottom→top with the original effect roll
  (`round(rand*10) + max(round(score/1000), 4)`; `≤7` static else moving).
- Moving platforms oscillate at `±1.5` and bounce 10px off each wall.
- Screen-edge wrap-around and sprite flip based on movement direction.
- Game over on falling off the bottom **or** landing on lava.
- Coordinate system kept y-down (top = 0) exactly like the original; the render
  layer flips to libGDX's y-up camera, running in a fixed `432×768` virtual
  world via a `FitViewport`.
- Lifetime distance / jumps, high-score record, the 15 "Conquistas" milestones
  and the Portuguese "Curiosidades" trivia are all reproduced.

**One intentional deviation, documented:** the original's lava logic in `jump()`
used `if (p.effects = 'lava')` — a single-`=` assignment bug that only ever
turned platform `p1` to lava, guarded by a clearly-intended "spawn lava only if
none exists" check. This port honours that *intent*: past score 600, a recycled
platform may roll into lava, capped at one lava platform on screen at a time
(see [`EffectPlatformFactory`](core/src/com/starbots/starjump/model/platform/EffectPlatformFactory.java)).
The original also shipped `swoosh.mp3` wired to jumps but left the call commented
out — here it's enabled through the Observer bus.

---

## Project layout

```
StarJumpLibGDX/
├── core/      shared game logic — all six patterns live here
├── lwjgl3/    desktop launcher (keyboard controls)
├── android/   android launcher (accelerometer / tilt) — included only when an SDK is present
└── assets/    sprites, fonts, sound (copied from the original)
```

---

## Build & run

Requires a JDK (17+). Everything else (Gradle, libGDX) is fetched by the wrapper.

### Desktop (keyboard)

```bash
./gradlew lwjgl3:run
```

**Controls:** `←/→` or `A/D` move • `SPACE`/`ENTER` start • `C` achievements •
`ESC` back • drag / `↑↓` scroll the curiosities list.

Build a runnable fat jar:

```bash
./gradlew lwjgl3:jar
java -jar lwjgl3/build/libs/StarJump-1.0.0.jar
```

### Android (tilt)

The `:android` module is only included when an Android SDK is configured. Add a
`local.properties` with `sdk.dir=/path/to/Android/sdk` (or export `ANDROID_HOME`),
then:

```bash
./gradlew android:assembleDebug          # build APK
./gradlew android:installDebug           # install on a connected device
```

Tilt the phone left/right to move; portrait orientation, accelerometer required.

---

## Credits

Original game **Star Jump** by [@developerdavi](https://github.com/developerdavi)
(Starbots / SESI-SENAI Betim FTC robotics team), MIT-licensed. Art, fonts, sound
and trivia are reused from the original project. This port is for educational
(design-patterns) purposes.
