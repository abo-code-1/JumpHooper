package com.starbots.starjump.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.input.TiltControl;
import com.starbots.starjump.model.boss.Boss;
import com.starbots.starjump.model.enemy.Enemy;
import com.starbots.starjump.model.enemy.EnemyFactory;
import com.starbots.starjump.model.enemy.StandardEnemyFactory;
import com.starbots.starjump.model.platform.EffectPlatformFactory;
import com.starbots.starjump.model.platform.PlainPlatformFactory;
import com.starbots.starjump.model.platform.Platform;
import com.starbots.starjump.model.platform.PlatformFactory;
import com.starbots.starjump.model.platform.PlatformKind;
import com.starbots.starjump.model.platform.SpawnContext;
import com.starbots.starjump.patterns.observer.EventBus;
import com.starbots.starjump.patterns.observer.GameEventType;
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
    private final EventBus bus;
    private final ScoreManager scores = ScoreManager.INSTANCE;

    private final PlatformFactory plainFactory = new PlainPlatformFactory();
    private final PlatformFactory effectFactory = new EffectPlatformFactory();
    private final SpawnContext spawnCtx = new SpawnContext();

    private final Player player = new Player();
    private final Platform[] platforms = new Platform[Config.PLATFORM_COUNT];

    private final EnemyFactory enemyFactory = new StandardEnemyFactory();
    private final Array<Enemy> enemies = new Array<>();

    private Boss boss;
    private int nextBossScore = FIRST_BOSS_SCORE;
    private final Array<Projectile> projectiles = new Array<>();
    private final Pool<Projectile> projectilePool = new Pool<Projectile>() {
        @Override protected Projectile newObject() { return new Projectile(); }
    };

    private final Array<Pickup> pickups = new Array<>();

    private int lives;
    private float invulnTimer;     // brief i-frames after a hit
    private float jetpackTimer;    // > 0 while flying

    private boolean gameOver;

    /** How far the world scrolled this step (>= 0); drives background parallax. */
    private float lastScrollDy;

    public World(TiltControl tilt, EventBus bus) {
        this.tilt = tilt;
        this.bus = bus;
    }

    /** Mirrors {@code reset()} + the initial entity layout from App.js. */
    public void startRun() {
        scores.startRun();
        player.reset();
        enemies.clear();
        pickups.clear();
        boss = null;
        nextBossScore = FIRST_BOSS_SCORE;
        projectilePool.freeAll(projectiles);
        projectiles.clear();
        lives = Config.START_LIVES;
        invulnTimer = 0f;
        jetpackTimer = 0f;
        gameOver = false;
        float max = Config.WORLD_WIDTH - Config.PLATFORM_W; // randomW(60)
        for (int i = 0; i < platforms.length; i++) {
            float x = MathUtils.random(0f, max);
            float y = 40f + i * Config.PLATFORM_SPACING; // evenly spaced down the screen
            platforms[i] = plainFactory.spawn(x, y, spawnCtx.set(0, false));
        }
    }

    /** One fixed simulation step (== one frame of the original gameLoop). */
    public void step() {
        if (gameOver) return;

        lastScrollDy = 0f;
        if (invulnTimer > 0) invulnTimer -= Config.STEP;

        // Jetpack overrides physics: a steady, fast climb.
        if (jetpackTimer > 0) {
            jetpackTimer -= Config.STEP;
            player.speed = Config.JETPACK_SPEED;
        }

        final float velocityX = tilt.getTilt();
        final float speed = player.speed; // captured at frame start, exactly like the original

        // Landing is only tested while falling (and not while jetpacking).
        if (speed > 0 && jetpackTimer <= 0) {
            for (int i = 0; i < platforms.length; i++) {
                Platform p = platforms[i];
                if (!CollisionSystem.lands(player, p)) continue;

                if (p.onLand(player)) {            // lava (Strategy flags it as a hazard)
                    // Costs a life rather than killing outright; then bounce off.
                    if (invulnTimer <= 0) loseLife();
                    if (!gameOver) {               // launch back off the lava
                        player.speed = Config.JUMP_BASE - (scores.getScore() / 6000f);
                    }
                    continue;
                }

                if (p.hasSpring) {
                    springBounce(p);
                } else {
                    jump();
                    if (p.getBehavior() != null && p.getBehavior().breaksOnLand()) {
                        // Cracked platform: bounce, then shatter and recycle.
                        // Pass the old platform so the FX can draw its real texture.
                        publish(GameEventType.PLATFORM_BROKEN, p);
                        float newX = MathUtils.random(0f, Config.WORLD_WIDTH - Config.PLATFORM_W);
                        platforms[i] = effectFactory.spawn(newX,
                                highestPlatformY() - Config.PLATFORM_SPACING,
                                spawnCtx.set(scores.getScore(), !anyLava()));
                    }
                }
            }
        }

        // Moving platforms slide (Strategy).
        for (Platform p : platforms) {
            p.update();
        }

        // Enemies move (Strategy) and interact with the player.
        updateEnemies();

        // Boss (State machine) + its projectiles.
        updateBoss();
        updateProjectiles();

        // Floating pickups (hearts, jetpacks).
        updatePickups();

        // Fell off the bottom? Always fatal — falling never spends a life.
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

    private static final int ENEMY_UNLOCK_SCORE = 1200;   // peaceful early game
    private static final int MAX_ENEMIES = 2;
    /** Contact is only lethal once the player has clearly overlapped the enemy. */
    private static final float HIT_INSET = 0.22f;

    /** Move enemies, despawn off-screen ones, and resolve player collisions. */
    private void updateEnemies() {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(player, Config.STEP);

            if (e.y > Config.WORLD_HEIGHT + 60) {     // drifted off the bottom
                enemies.removeIndex(i);
                continue;
            }

            // An enemy is only dangerous once it has fully scrolled into view,
            // so nothing can kill you from off-screen above.
            if (e.y >= 0 && overlaps(e)) {
                // Generous "from above" stomp: any descending contact near the
                // upper half counts as a bounce rather than a death.
                boolean stomp = player.speed > 0 && player.bottom() <= e.centerY() + e.height * 0.3f;
                if (stomp || jetpackTimer > 0) {
                    if (stomp) jump();                 // bounce off it (jetpack just plows through)
                    scores.addScore(50);
                    publish(GameEventType.ENEMY_KILLED, new Vector2(e.centerX(), e.centerY()));
                    enemies.removeIndex(i);
                } else if (invulnTimer <= 0) {
                    // A side/below hit costs a life (not an instant death).
                    publish(GameEventType.ENEMY_KILLED, new Vector2(e.centerX(), e.centerY()));
                    enemies.removeIndex(i);
                    loseLife();
                }
            }
        }
    }

    /** Lose a life from a creature hit; game over only when none remain. */
    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
        } else {
            invulnTimer = Config.INVULN_TIME;
            player.speed = -8f;                    // small recovery pop
            publish(GameEventType.LIFE_LOST, lives);
        }
    }

    private boolean overlaps(Enemy e) {
        // Shrink the enemy's lethal box so glancing near-misses don't kill.
        float mx = e.width * HIT_INSET;
        float my = e.height * HIT_INSET;
        return player.x < e.right() - mx
            && player.x + player.width > e.x + mx
            && player.y < e.bottom() - my
            && player.bottom() > e.y + my;
    }

    // --- boss ------------------------------------------------------------------

    private static final int FIRST_BOSS_SCORE = 2800;
    private static final int BOSS_RESPAWN_GAP = 4000;
    private static final int BOSS_HP = 4;
    // Keep the boss reachable: it straddles the player's rest line (scroll
    // threshold) so a rising bounce connects while shots still rain downward.
    private static final float BOSS_HOVER_Y = Config.SCROLL_THRESHOLD - 66f;
    private static final float BOSS_BOUNCE = 7f;

    /** Spawn / advance the boss; the player damages it by bouncing into it. */
    private void updateBoss() {
        if (boss == null) {
            if (scores.getScore() >= nextBossScore) {
                boss = new Boss(BOSS_HP, BOSS_HOVER_Y);
                publish(GameEventType.BOSS_SPAWNED, new Vector2(boss.centerX(), boss.centerY()));
            }
            return;
        }

        boss.update(this, Config.STEP);

        if (boss.entered && boss.hitCooldown <= 0 && playerHitsBoss()) {
            boss.hp--;
            boss.flash = 0.16f;
            boss.hitCooldown = 0.45f;
            player.speed = BOSS_BOUNCE; // knocked back downward
            if (boss.hp <= 0) {
                publish(GameEventType.BOSS_DEFEATED, new Vector2(boss.centerX(), boss.centerY()));
                scores.addScore(500);
                nextBossScore = scores.getScore() + BOSS_RESPAWN_GAP;
                boss = null;
            } else {
                publish(GameEventType.BOSS_HIT, new Vector2(boss.centerX(), boss.centerY()));
            }
        }
    }

    private boolean playerHitsBoss() {
        return player.x < boss.right()
            && player.x + player.width > boss.x
            && player.y < boss.bottom()
            && player.bottom() > boss.y;
    }

    /** Spawned by the boss states via {@link com.starbots.starjump.model.boss.BossState}. */
    public void spawnProjectile(float x, float y, float vx, float vy) {
        Projectile pr = projectilePool.obtain();
        pr.x = x; pr.y = y; pr.vx = vx; pr.vy = vy;
        projectiles.add(pr);
    }

    private void updateProjectiles() {
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile pr = projectiles.get(i);
            pr.x += pr.vx * Config.STEP;
            pr.y += pr.vy * Config.STEP;

            boolean offscreen = pr.y > Config.WORLD_HEIGHT + 30
                    || pr.x < -30 || pr.x > Config.WORLD_WIDTH + 30;
            if (offscreen) {
                projectilePool.free(pr);
                projectiles.removeIndex(i);
                continue;
            }
            // Circle (projectile) vs AABB (player): costs a life (boss damage).
            boolean hit = player.x < pr.x + pr.radius && player.x + player.width > pr.x - pr.radius
                    && player.y < pr.y + pr.radius && player.bottom() > pr.y - pr.radius;
            if (hit) {
                projectilePool.free(pr);
                projectiles.removeIndex(i);
                if (invulnTimer <= 0 && jetpackTimer <= 0) loseLife();
            }
        }
    }

    /** Ported from {@code jump()} but with a much stronger spring impulse. */
    private void springBounce(Platform p) {
        player.speed = Config.SPRING_SPEED;
        scores.registerJump();
        publish(GameEventType.SPRING_BOUNCE, new Vector2(p.centerX(), p.y));
    }

    // --- pickups (hearts, jetpacks) -------------------------------------------

    private static final int MAX_PICKUPS = 2;

    private void updatePickups() {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup pk = pickups.get(i);
            pk.bobPhase += Config.STEP * 4f;
            if (pk.y > Config.WORLD_HEIGHT + 60) {
                pickups.removeIndex(i);
                continue;
            }
            boolean overlap = player.x < pk.right() && player.x + player.width > pk.x
                    && player.y < pk.bottom() && player.bottom() > pk.y;
            if (overlap) {
                collect(pk);
                pickups.removeIndex(i);
            }
        }
    }

    private void collect(Pickup pk) {
        Vector2 pos = new Vector2(pk.centerX(), pk.centerY());
        if (pk.type == Pickup.Type.HEART) {
            if (lives < Config.MAX_LIVES) lives++;
            else scores.addScore(150);             // already full -> bonus points
            publish(GameEventType.LIFE_GAINED, lives);
        } else { // JETPACK
            jetpackTimer = Config.JETPACK_DURATION;
            player.speed = Config.JETPACK_SPEED;
            publish(GameEventType.JETPACK_START, pos);
        }
    }

    private void publish(GameEventType type, Object payload) {
        if (bus != null) bus.publish(type, payload);
    }

    /** Ported from {@code up()}: scroll platforms down, recycle, add score. */
    private void scrollWorld(float speed) {
        float dy = speed * Config.SCROLL_FACTOR; // speed<0 * -1.2 => moves down (>0)
        lastScrollDy = dy;
        float max = Config.WORLD_WIDTH - Config.PLATFORM_W;
        for (int i = 0; i < platforms.length; i++) {
            Platform p = platforms[i];
            p.y += speed * Config.SCROLL_FACTOR; // speed<0 * -1.2 => moves down
            if (p.y > Config.WORLD_HEIGHT) {
                // Recycle one uniform gap above the current highest platform, so
                // spacing stays even and the next platform is always reachable.
                float newX = MathUtils.random(0f, max);
                float newY = highestPlatformY() - Config.PLATFORM_SPACING;
                boolean lavaAllowed = !anyLava();
                platforms[i] = effectFactory.spawn(newX, newY,
                        spawnCtx.set(scores.getScore(), lavaAllowed));
            }
        }
        // Enemies + pickups scroll with the world.
        for (Enemy e : enemies) {
            e.y += dy;
        }
        for (Pickup pk : pickups) {
            pk.y += dy;
        }

        // Occasionally spawn a new enemy from the top once unlocked
        // (suppressed while a boss is on screen). Spawned high above the screen
        // so it scrolls into view gradually instead of ambushing the player.
        if (boss == null && scores.getScore() > ENEMY_UNLOCK_SCORE && enemies.size < MAX_ENEMIES
                && MathUtils.random() < 0.005f) {
            float ex = MathUtils.random(0f, Config.WORLD_WIDTH - 46f);
            enemies.add(enemyFactory.spawn(ex, -90f, scores.getScore()));
        }

        // Floating pickups drift down from the top: hearts (more likely when hurt)
        // and the rarer jetpack.
        if (pickups.size < MAX_PICKUPS) {
            float heartChance = lives < Config.MAX_LIVES ? 0.0035f : 0.0008f;
            if (MathUtils.random() < heartChance) {
                float px = MathUtils.random(20f, Config.WORLD_WIDTH - 54f);
                pickups.add(new Pickup(Pickup.Type.HEART, px, -40f));
            } else if (MathUtils.random() < 0.0016f) {
                float px = MathUtils.random(20f, Config.WORLD_WIDTH - 50f);
                pickups.add(new Pickup(Pickup.Type.JETPACK, px, -50f));
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

    /** The smallest y (highest on screen) among the live platforms. */
    private float highestPlatformY() {
        float min = Float.MAX_VALUE;
        for (Platform p : platforms) {
            if (p != null && p.y < min) min = p.y;
        }
        return min;
    }

    public Player getPlayer()                { return player; }
    public Platform[] getPlatforms()         { return platforms; }
    public Array<Enemy> getEnemies()         { return enemies; }
    public Array<Pickup> getPickups()        { return pickups; }
    public Boss getBoss()                    { return boss; }
    public Array<Projectile> getProjectiles(){ return projectiles; }
    public boolean isGameOver()              { return gameOver; }
    public float getLastScrollDy()           { return lastScrollDy; }
    public int getLives()                    { return lives; }
    public int getMaxLives()                 { return Config.MAX_LIVES; }
    public float getJetpackTimer()           { return jetpackTimer; }
    public boolean isJetpackActive()         { return jetpackTimer > 0; }
    public boolean isInvulnerable()          { return invulnTimer > 0; }
}
