---
name: Design pattern implementation
description: Implement one of the 6 MVP design patterns correctly, in the canonical location, with the right visibility.
triggers: [implement singleton, implement factory, implement strategy, implement observer, implement state, implement adapter, add pattern, apply pattern]
---

# Pattern implementation skill

If the user asks for "the singleton" etc., these are the canonical shapes for this repo. Follow them to preserve grading visibility.

## Singleton — `ScoreManager`

Java's **enum singleton** (Effective Java Item 3 — thread-safe, reflection-safe, serialization-safe).

```java
/** Singleton (enum) — shared score + Preferences-backed high score. */
public enum ScoreManager {
    INSTANCE;
    private static final String KEY_HIGH = "highScore";
    private final Preferences prefs = Gdx.app.getPreferences("duddlejump");
    private int current = 0;

    public void reset()   { current = 0; }
    public int  current() { return current; }
    public int  getHigh() { return prefs.getInteger(KEY_HIGH, 0); }
    public void add(int n) {
        current += n;
        if (current > getHigh()) prefs.putInteger(KEY_HIGH, current).flush();
    }
}
```

## Factory Method — `PlatformFactory`

```java
/** Factory Method — builds platforms without exposing concrete types. */
public final class PlatformFactory {
    public enum Type { GREEN, RED, BLUE, WHITE }
    public static Platform create(Type t, float x, float y) {
        switch (t) {
            case GREEN: return new GreenPlatform(x, y);
            case RED:   return new RedPlatform(x, y);
            case BLUE:  return new BluePlatform(x, y);
            case WHITE: return new WhitePlatform(x, y);
            default:    throw new IllegalArgumentException(t.name());
        }
    }
}
```

**Rule:** the switch lives **here and nowhere else**.

## Strategy — `Platform.onContact(Doodle)`

```java
public abstract class Platform {
    /** Strategy — each subclass supplies its contact reaction. */
    public abstract void onContact(Doodle doodle);
    public void update(float dt) { /* default no-op */ }
}

public class GreenPlatform extends Platform {
    @Override public void onContact(Doodle d) { d.bounce(); }
}
public class RedPlatform extends Platform {
    @Override public void onContact(Doodle d) { d.bounce(); markDestroyed(); }
}
```

**Rule:** no `instanceof`, no `switch(type)` outside the factory.

## Observer — `EventBus` + `ContactListener`

```java
public interface ContactListener { void onContact(Doodle d, Platform p); }

public final class EventBus {
    private final Array<ContactListener> listeners = new Array<>();
    public void subscribe(ContactListener l)   { listeners.add(l); }
    public void unsubscribe(ContactListener l) { listeners.removeValue(l, true); }
    public void publishContact(Doodle d, Platform p) {
        for (int i = 0; i < listeners.size; i++) listeners.get(i).onContact(d, p);
    }
}
```

**Rule:** `GameScreen.show()` subscribes, `hide()` / `dispose()` unsubscribes. Leaks otherwise.

## State — `GameState`

```java
public interface GameState {
    void enter(); void update(float dt); void render(SpriteBatch b); void exit();
}

// in GameScreen:
private GameState current;
public void setState(GameState next) {
    if (current != null) current.exit();
    current = next;
    current.enter();
}
```

Three impls: `PlayingState`, `PausedState`, `GameOverState`. Transitions live **inside states**, not in `GameScreen`.

**Rule:** no `isPaused` / `isGameOver` booleans in `GameScreen`.

## Adapter — `InputController`

```java
public interface InputController { float getHorizontal(); /* [-1, 1] */ }

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
```

Pick once at startup:

```java
InputController input = (Gdx.app.getType() == ApplicationType.Android)
    ? new AccelerometerInputController()
    : new KeyboardInputController();
```

**Rule:** no `if (android)` branches in `Doodle` — Adapter handles it.
