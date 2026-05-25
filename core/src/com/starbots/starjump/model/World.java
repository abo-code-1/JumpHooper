package com.starbots.starjump.model;

import com.badlogic.gdx.math.MathUtils;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.input.TiltControl;
import com.starbots.starjump.model.platform.EffectPlatformFactory;
import com.starbots.starjump.model.platform.PlainPlatformFactory;
import com.starbots.starjump.model.platform.Platform;
import com.starbots.starjump.model.platform.PlatformFactory;
import com.starbots.starjump.model.platform.PlatformKind;
import com.starbots.starjump.model.platform.SpawnContext;
import com.starbots.starjump.physics.CollisionSystem;

/**
 * The game simulation. This is the faithful port of the original
 * {@code systems.js} {@code Controller} ({@code gameLoop}, {@code jump},
 * {@code up}, {@code animateEffects}, {@code reset}), advanced one fixed 60 Hz
 * step at a time so the ported per-frame constants behave identically.
 *
 * <p>It is a pure model: it owns no textures and does no drawing. It pulls
 * input through a {@link TiltControl} (Adapter), builds platforms through
 * {@link PlatformFactory} (Factory Method) whose products carry behaviour
 * Strategies, and drives the {@link ScoreManager} (Singleton) — which is the
 * subject that broadcasts score / jump / death events on the bus (Observer).</p>
 */
public final class World {

    private final TiltControl tilt;
    private final ScoreManager scores = ScoreManager.INSTANCE;

    private final PlatformFactory plainFactory = new PlainPlatformFactory();
    private final PlatformFactory effectFactory = new EffectPlatformFactory();
    private final SpawnContext spawnCtx = new SpawnContext();

    private final Player player = new Player();
    private final Platform[] platforms = new Platform[Config.PLATFORM_COUNT];

    private boolean gameOver;

    public World(TiltControl tilt) {
        this.tilt = tilt;
    }

    /** Mirrors {@code reset()} + the initial entity layout from App.js. */
    public void startRun() {
        scores.startRun();
        player.reset();
        gameOver = false;
        float max = Config.WORLD_WIDTH - Config.PLATFORM_W; // randomW(60)
        for (int i = 0; i < platforms.length; i++) {
            float x = MathUtils.random(0f, max);
            float y = Config.PLATFORM_SPACING * (i + 1); // 60, 120, ... 540
            platforms[i] = plainFactory.spawn(x, y, spawnCtx.set(0, false));
        }
    }

    /** One fixed simulation step (== one frame of the original gameLoop). */
    public void step() {
        if (gameOver) return;

        final float velocityX = tilt.getTilt();
        final float speed = player.speed; // captured at frame start, exactly like the original

        // Landing is only tested while falling.
        if (speed > 0) {
            for (Platform p : platforms) {
                if (CollisionSystem.lands(player, p)) {
                    if (p.onLand(player)) {  // lava -> fatal (Strategy decides)
                        gameOver = true;
                    }
                    jump();
                }
            }
        }

        // Moving platforms slide (Strategy).
        for (Platform p : platforms) {
            p.update();
        }

        // Fell off the bottom?
        if (player.y > Config.WORLD_HEIGHT) {
            gameOver = true;
        }

        // Scroll the world while ascending near the top; otherwise move the player.
        if (player.y < Config.SCROLL_THRESHOLD && speed < 0) {
            scrollWorld(speed);
        } else {
            player.y += speed;
        }

        // Horizontal movement, facing + screen-edge wrap-around.
        if (velocityX > 0) {                 // tilt makes the astronaut go left
            player.facingLeft = true;
            if (player.x + player.width <= 0) {
                player.x = Config.WORLD_WIDTH - 1;
            }
        } else if (velocityX < 0) {          // ... go right
            player.facingLeft = false;
            if (player.x >= Config.WORLD_WIDTH) {
                player.x = -player.width;
            }
        }
        player.x += velocityX * Config.MOVE_FACTOR;

        // Gravity.
        player.speed += Config.GRAVITY;
    }

    /** Ported from {@code jump()}: bounce impulse grows slightly with score. */
    private void jump() {
        player.speed = Config.JUMP_BASE - (scores.getScore() / 6000f);
        scores.registerJump();
    }

    /** Ported from {@code up()}: scroll platforms down, recycle, add score. */
    private void scrollWorld(float speed) {
        float max = Config.WORLD_WIDTH - Config.PLATFORM_W;
        for (int i = 0; i < platforms.length; i++) {
            Platform p = platforms[i];
            p.y += speed * Config.SCROLL_FACTOR; // speed<0 * -1.2 => moves down
            if (p.y > Config.WORLD_HEIGHT) {
                float newX = MathUtils.random(0f, max);
                float newY = -Config.PLATFORM_H; // -h, just above the top
                boolean lavaAllowed = !anyLava();
                platforms[i] = effectFactory.spawn(newX, newY,
                        spawnCtx.set(scores.getScore(), lavaAllowed));
            }
        }
        // score += -speed/5, rounded each frame (see ScoreManager).
        scores.addScore(Math.round(-speed / 5f));
    }

    private boolean anyLava() {
        for (Platform p : platforms) {
            if (p != null && p.kind == PlatformKind.LAVA) return true;
        }
        return false;
    }

    public Player getPlayer()        { return player; }
    public Platform[] getPlatforms() { return platforms; }
    public boolean isGameOver()      { return gameOver; }
}
