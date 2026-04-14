# DuddleJump — Architecture & Design Patterns (Java / libGDX)

Authoritative reference for *why* the code is structured the way it is.

Audience: Design Pattern course graders + future contributors.

---

## Table of contents

1. [Runtime lifecycle](#1-runtime-lifecycle)
2. [Entity interaction diagram](#2-entity-interaction-diagram)
3. [Sequence: "Doodle lands on a platform"](#3-sequence-doodle-lands-on-a-platform)
4. [State diagram: `GameScreen`](#4-state-diagram-gamescreen)
5. [Design patterns — deep dive](#5-design-patterns--deep-dive)
   - 5.1 Singleton — `ScoreManager`
   - 5.2 Factory Method — `PlatformFactory`
   - 5.3 Strategy — platform on-contact + input
   - 5.4 Observer — `EventBus` / `ContactListener`
   - 5.5 State — `GameState` hierarchy
   - 5.6 Adapter — `InputController`
6. [Physics & collision](#6-physics--collision)
7. [Camera & infinite world](#7-camera--infinite-world)
8. [Screen boundaries & parameter passing](#8-screen-boundaries--parameter-passing)
9. [Known trade-offs](#9-known-trade-offs)
10. [Testing strategy](#10-testing-strategy)

---

## 1. Runtime lifecycle

```
DesktopLauncher.main()
   └── new Lwjgl3Application(new DuddleJumpGame(), config)
         └── DuddleJumpGame.create()
               ├── Assets.load()            (libGDX AssetManager)
               └── setScreen(new MainMenuScreen(game))
                     ↓ "Play" clicked
                  setScreen(new GameScreen(game))
                     ↓ Doodle falls below camera
                  setScreen(new GameOverScreen(game, score))
                     ↓ "Restart" clicked
                  setScreen(new GameScreen(game))
```

All screens extend `com.badlogic.gdx.ScreenAdapter`. Payloads travel through **constructors**, never through statics.

---

## 2. Entity interaction diagram

High-level view of who talks to whom and through what pattern.

```
                         ┌───────────────────────┐
                         │   DuddleJumpGame      │  ← Game (libGDX)
                         │   (screen switcher)   │
                         └───────────┬───────────┘
                                     │ setScreen(...)
             ┌───────────────────────┼───────────────────────┐
             ▼                       ▼                       ▼
 ┌───────────────────┐   ┌───────────────────┐   ┌────────────────────┐
 │ MainMenuScreen    │   │   GameScreen      │   │ GameOverScreen     │
 └───────────────────┘   │  (hosts game loop)│   └─────────┬──────────┘
                         └───────┬───────────┘             │
                                 │                         │ reads
                                 │ holds                   ▼
                                 │            ┌───────────────────────┐
                                 │            │   ScoreManager        │
                                 │   writes   │   (Singleton, enum)   │
                                 │ ───────────┼─► wraps Preferences   │
                                 │            └───────────────────────┘
                                 │
                                 │ owns
     ┌───────────────────────────┼──────────────────────────────────┐
     │                           │                                  │
     ▼                           ▼                                  ▼
┌─────────────┐        ┌──────────────────┐               ┌─────────────────┐
│  Doodle     │◄──────►│ InputController  │               │ PlatformFactory │
│  (player)   │ uses   │  (interface)     │               │  (static util)  │
└──────┬──────┘        └──────────────────┘               └────────┬────────┘
       │                   ▲        ▲                              │
       │                   │        │                          creates
       │ emits             │impl    │impl                          │
       │ contact           │        │                              ▼
       │            ┌──────┴───┐ ┌──┴───────────┐       ┌─────────────────────┐
       │            │ Keyboard │ │ Accelerometer│       │  Platform (abstract)│
       │            │ Ctrl     │ │ Ctrl         │       │  ──────────────────│
       │            └──────────┘ └──────────────┘       │  + onContact(d)    │◄── Strategy
       │                                                │  + update(dt)      │
       │                                                └─────────┬───────────┘
       │                                                          │
       │                                   ┌───────────┬──────────┼──────────┬────────────┐
       │                                   ▼           ▼          ▼          ▼            │
       │                         ┌─────────────┐ ┌──────────┐ ┌─────────┐ ┌─────────┐    │
       │                         │   Green     │ │   Red    │ │  Blue   │ │  White  │    │
       │                         └─────────────┘ └──────────┘ └─────────┘ └─────────┘    │
       │                                                                                  │
       │                            ┌───────────────────────────────────────┐             │
       └───────────────────────────►│   EventBus                            │             │
         publishContact(d, p)       │   list<ContactListener>               │             │
                                    │   + subscribe / unsubscribe           │             │
                                    │   + publishContact(d, p)              │◄── Observer │
                                    └──────────────┬────────────────────────┘             │
                                                   │ notifies                             │
                 ┌─────────────────┬───────────────┼──────────────┐                       │
                 ▼                 ▼               ▼              ▼                       │
          ┌──────────────┐ ┌──────────────┐ ┌─────────────┐ ┌──────────────┐              │
          │ ScoreHUD     │ │ SoundPlayer  │ │ScreenShake  │ │ PlayingState │─────────────┘
          │ (Actor)      │ │              │ │             │ │ (calls       │  delegates onContact
          └──────────────┘ └──────────────┘ └─────────────┘ │  p.onContact)│
                                                            └──────────────┘
                                                                    ▲
                                                                    │ current state
                                                            ┌───────┴───────┐
                                                            │ GameState     │◄── State
                                                            │ (interface)   │
                                                            └───┬───┬───┬───┘
                                                                │   │   │
                                              ┌─────────────────┘   │   └──────────────────┐
                                              ▼                     ▼                      ▼
                                      ┌──────────────┐     ┌──────────────┐        ┌─────────────────┐
                                      │ PlayingState │     │ PausedState  │        │ GameOverState   │
                                      └──────────────┘     └──────────────┘        └─────────────────┘
```

**Legend for arrows:**
- **solid →** direct reference / method call
- **"emits" →** fires an event; zero knowledge of who listens
- **"impl" / inheritance** → `extends` / `implements`

**Pattern annotations on the diagram:**

| Arrow / box | Pattern |
|---|---|
| `ScoreManager` (enum) | **Singleton** |
| `PlatformFactory` → subclasses | **Factory Method** |
| `Platform.onContact()` polymorphism | **Strategy** |
| `Doodle → EventBus → listeners` | **Observer** |
| `GameScreen ↔ GameState` | **State** |
| `InputController` interface | **Adapter** + **Strategy** |

---

## 3. Sequence: "Doodle lands on a platform"

This is the central gameplay loop. Every frame while playing, this sequence *may* fire.

```
Doodle      EventBus        PlayingState      Platform        ScoreManager     ScoreHUD
  │             │                 │              │                   │             │
  │ update(dt)                    │              │                   │             │
  │──────────────────────────────►│              │                   │             │
  │             │                 │              │                   │             │
  │ checkContact(platforms, bus)  │              │                   │             │
  │◄──────────────────────────────│              │                   │             │
  │             │                 │              │                   │             │
  │ (finds overlap, v.y < 0)      │              │                   │             │
  │             │                 │              │                   │             │
  │ publishContact(this, p)       │              │                   │             │
  │────────────►│                 │              │                   │             │
  │             │                 │              │                   │             │
  │             │ onContact(d, p) │              │                   │             │
  │             │────────────────►│              │                   │             │
  │             │                 │              │                   │             │
  │             │                 │ p.onContact(d)                   │             │
  │             │                 │─────────────►│                   │             │
  │             │                 │              │ d.bounce()        │             │
  │             │                 │              │ [+ type-specific: │             │
  │             │                 │              │  destroy / fade / │             │
  │             │                 │              │  nothing]         │             │
  │             │                 │              │                   │             │
  │             │                 │ ScoreManager.INSTANCE.add(1)     │             │
  │             │                 │─────────────────────────────────►│             │
  │             │                 │                                  │             │
  │             │ onContact(d, p) (also notifies HUD/sound/shake)    │             │
  │             │───────────────────────────────────────────────────────────────►  │
  │             │                                                    │             │
  │             │                                                    │ refresh "score" label
  │             │                                                    │             │
  │◄────────────────────────────────────────────────────────────────────────────── │
  │             │                 │              │                   │             │
```

Key points:
- Doodle has **no idea** what a `GreenPlatform` is. It just publishes a `contact` event.
- `PlayingState` routes the event to `platform.onContact(doodle)` (Strategy dispatch) and also increments score.
- `ScoreHUD`, `SoundPlayer`, etc. are independent subscribers — adding one new listener never touches existing code (Open/Closed).

---

## 4. State diagram: `GameScreen`

```
              ┌──────────────────┐
              │  (screen entry)  │
              └────────┬─────────┘
                       ▼
                ┌──────────────┐
      ┌────────►│ PlayingState │────────┐
      │         └──────┬───────┘        │
      │                │                │
      │ P key          │ Doodle.y < camLow
      │                ▼                ▼
┌─────┴──────┐   ┌──────────────┐  ┌──────────────────┐
│ PausedState│◄──┤ (P toggles)  │  │ GameOverState    │───► setScreen(GameOverScreen)
└────────────┘                     └──────────────────┘
```

Each state owns its own `update()`, `render()`, `enter()`, `exit()`. `GameScreen.render(delta)` is a dumb host:

```java
@Override public void render(float delta) {
    current.update(delta);
    batch.begin();
    current.render(batch);
    batch.end();
}
```

Transitions are triggered *from within* states (e.g. `PlayingState` detects the fall), so no external code can cause illegal state changes.

---

## 5. Design patterns — deep dive

### 5.1 Singleton — `ScoreManager`

**Problem:** multiple screens need the score; high score must survive app restarts.

**Solution:** classic enum singleton (thread-safe, reflection-safe, serialization-safe — Effective Java Item 3).

```java
public enum ScoreManager {
    INSTANCE;

    private static final String KEY_HIGH = "highScore";
    private final Preferences prefs = Gdx.app.getPreferences("duddlejump");
    private int current = 0;

    public void reset()     { current = 0; }
    public int  current()   { return current; }
    public int  getHigh()   { return prefs.getInteger(KEY_HIGH, 0); }
    public void add(int n) {
        current += n;
        if (current > getHigh()) prefs.putInteger(KEY_HIGH, current).flush();
    }
}
```

**Why not a static utility class?** Because `ScoreManager` owns real *state* (`current`) and *resources* (`Preferences`), which break in test environments unless isolated behind an instance.

---

### 5.2 Factory Method — `PlatformFactory`

**Problem:** four platform subclasses, callers shouldn't care about concrete types.

```java
public final class PlatformFactory {
    public enum Type { GREEN, RED, BLUE, WHITE }

    public static Platform create(Type type, float x, float y) {
        switch (type) {
            case GREEN: return new GreenPlatform(x, y);
            case RED:   return new RedPlatform(x, y);
            case BLUE:  return new BluePlatform(x, y);
            case WHITE: return new WhitePlatform(x, y);
            default:    throw new IllegalArgumentException(type.name());
        }
    }

    public static Platform random(float x, float y, float whiteProb, float redProb, float blueProb) {
        float r = MathUtils.random();
        if ((r -= whiteProb) < 0) return create(Type.WHITE, x, y);
        if ((r -= redProb)   < 0) return create(Type.RED,   x, y);
        if ((r -= blueProb)  < 0) return create(Type.BLUE,  x, y);
        return create(Type.GREEN, x, y);
    }
}
```

Adding a 5th type ("spring"): add enum value, add case, add subclass. Zero changes to `GameScreen`.

---

### 5.3 Strategy — platform on-contact + input

**Problem A:** each platform reacts differently when Doodle lands.

**Solution:** abstract method polymorphism — each subclass *is* a strategy.

```java
public abstract class Platform {
    protected final Rectangle bounds = new Rectangle();
    public  Rectangle getBounds() { return bounds; }
    public  void      update(float dt) { /* default no-op */ }
    public  abstract void onContact(Doodle doodle);
}

public class GreenPlatform extends Platform {
    @Override public void onContact(Doodle d) { d.bounce(); }
}
public class RedPlatform extends Platform {
    @Override public void onContact(Doodle d) { d.bounce(); markDestroyed(); }
}
public class BluePlatform extends Platform {
    private float vx = 80f;
    @Override public void update(float dt) {
        bounds.x += vx * dt;
        if (bounds.x < 0 || bounds.x + bounds.width > Config.WORLD_WIDTH) vx = -vx;
    }
    @Override public void onContact(Doodle d) { d.bounce(); }
}
public class WhitePlatform extends Platform {
    @Override public void onContact(Doodle d) { d.bounce(); fadeOut(); }
}
```

**Problem B:** reading horizontal input is different on desktop vs Android. Same Strategy interface (`InputController`) solves it — see 5.6.

---

### 5.4 Observer — `EventBus` / `ContactListener`

**Problem:** HUD, audio, achievements, particles — they all care when Doodle touches a platform, but neither Doodle nor GameScreen should know they exist.

```java
public interface ContactListener {
    void onContact(Doodle doodle, Platform platform);
}

public final class EventBus {
    private final Array<ContactListener> listeners = new Array<>();
    public void subscribe(ContactListener l)   { listeners.add(l); }
    public void unsubscribe(ContactListener l) { listeners.removeValue(l, true); }
    public void publishContact(Doodle d, Platform p) {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onContact(d, p);
    }
}
```

`GameScreen.show()` subscribes HUD, sound, and `PlayingState`; `hide()` / `dispose()` unsubscribes them to prevent leaks across screen switches.

---

### 5.5 State — `GameState` hierarchy

**Problem:** boolean flags (`isPaused`, `isGameOver`) explode combinatorially; allow illegal states like "game-over + paused".

**Solution:** one state object at a time; each owns its own behavior and transitions (see §4 for diagram).

```java
public interface GameState {
    void enter();
    void update(float dt);
    void render(SpriteBatch batch);
    void exit();
}

public class PlayingState implements GameState { /* ... */ }
public class PausedState   implements GameState { /* ... */ }
public class GameOverState implements GameState { /* ... */ }

// inside GameScreen:
private GameState current;
public void setState(GameState next) {
    if (current != null) current.exit();
    current = next;
    current.enter();
}
```

---

### 5.6 Adapter — `InputController`

**Problem:** desktop polls `Gdx.input.isKeyPressed(...)`; Android polls `Gdx.input.getAccelerometerX()`. Very different APIs, one common need: "give me horizontal intent in [-1, 1]".

```java
public interface InputController {
    float getHorizontal();     // [-1, 1]
}

public class KeyboardInputController implements InputController {
    @Override public float getHorizontal() {
        float x = 0f;
        if (Gdx.input.isKeyPressed(Keys.LEFT)  || Gdx.input.isKeyPressed(Keys.A)) x -= 1f;
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) x += 1f;
        return x;
    }
}

public class AccelerometerInputController implements InputController {
    @Override public float getHorizontal() {
        return MathUtils.clamp(-Gdx.input.getAccelerometerX() / 5f, -1f, 1f);
    }
}

// DuddleJumpGame.create()
InputController input = (Gdx.app.getType() == ApplicationType.Android)
    ? new AccelerometerInputController()
    : new KeyboardInputController();
```

Why Adapter vs just Strategy: the *intent* is to **adapt two incompatible APIs** (keyboard vs motion sensor) to a uniform interface; the fact that it's swappable at runtime is secondary. GoF would classify this primarily as Adapter.

---

## 6. Physics & collision

Hand-rolled, no Box2D at MVP scale.

| Field | Purpose |
|---|---|
| `velocity` (Vector2) | updates position each frame |
| `GRAVITY = (0, -2000)` | constant acceleration added to velocity |
| `HORIZONTAL_ACCEL` | input-driven acceleration added to `velocity.x` |

```java
public void update(float dt) {
    velocity.mulAdd(GRAVITY, dt);
    velocity.x += input.getHorizontal() * Config.HORIZONTAL_ACCEL * dt;
    bounds.x += velocity.x * dt;
    bounds.y += velocity.y * dt;
    wrapHorizontally();   // exit right → reappear left
}
```

Collision is AABB between a **1-px-tall contact strip** at Doodle's feet and platform bounds:

```java
public void checkContact(Array<Platform> platforms, EventBus bus) {
    if (velocity.y >= 0) return;                      // only while falling
    contactStrip.set(bounds.x, bounds.y, bounds.width, 1f);
    for (int i = 0; i < platforms.size; i++) {
        if (Intersector.overlaps(contactStrip, platforms.get(i).getBounds())) {
            bus.publishContact(this, platforms.get(i));
            return;
        }
    }
}
```

The `velocity.y >= 0` guard is what makes the game feel like a jump-through platformer.

---

## 7. Camera & infinite world

- `OrthographicCamera` with virtual resolution **480 × 800** via `FitViewport`.
- When Doodle crosses the vertical midpoint, camera follows upward; despawn any platform whose `y < camera.position.y - viewport.worldHeight`.
- `PlatformSpawner` keeps a minimum density above the camera by calling `PlatformFactory.random(...)` to fill gaps.

---

## 8. Screen boundaries & parameter passing

Never use static globals to pass data between screens.

```java
// ✅ right
game.setScreen(new GameOverScreen(game, ScoreManager.INSTANCE.current()));

// ❌ wrong
GameOverScreen.lastScore = this.score;
```

Each screen's constructor is its contract. `ScoreManager` is the **one** exception because it represents shared persistent state, not transient screen data.

---

## 9. Known trade-offs

- **Hand-rolled physics.** Good enough for MVP. If we add springs, monsters, or rope swings, migrate to Box2D.
- **Single texture atlas.** TexturePacker-generated `.atlas` — easy to ship, rigid to iterate. Acceptable for 10 MB of art.
- **No `Pool<Platform>` at MVP.** GC pressure is low at ~30 platforms on screen at 60 FPS. Add pooling only if a profiler flags it.
- **Enum singleton.** Used *only* for `ScoreManager`. Every other class is dependency-injected via constructor — we're not blanket-endorsing singletons.
- **Input picked once at startup.** If a user plugs in a keyboard to an Android device mid-game, we'd need to rebuild the `InputController`. Out of scope.

---

## 10. Testing strategy

- **Unit (JUnit 5):**
  - `PlatformFactory.create()` dispatch for each enum value.
  - `ScoreManager` arithmetic + high-score replacement, with `Preferences` mocked.
  - `KeyboardInputController` mapping (via `Gdx.input` mock) — LEFT/RIGHT/A/D/neutral combos.
- **Headless integration:** `HeadlessApplicationConfiguration` boot of `GameScreen` with a stub `InputController` that forces Doodle rightward; run ~500 ticks; assert no exceptions and score increases.
- **Manual:** all 4 platform behaviors, keyboard + accelerometer, pause/resume, restart loop, high-score persistence across app restart.
