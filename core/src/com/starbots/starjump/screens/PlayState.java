package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.fx.BreakingPlatformFx;
import com.starbots.starjump.fx.ParticleSystem;
import com.starbots.starjump.fx.ScreenShake;
import com.starbots.starjump.fx.SpaceBackground;
import com.starbots.starjump.model.Pickup;
import com.starbots.starjump.model.Player;
import com.starbots.starjump.model.Projectile;
import com.starbots.starjump.model.World;
import com.starbots.starjump.model.boss.Boss;
import com.starbots.starjump.model.enemy.Enemy;
import com.starbots.starjump.model.enemy.EnemyType;
import com.starbots.starjump.model.platform.Platform;
import com.starbots.starjump.patterns.observer.EventBus;
import com.starbots.starjump.patterns.observer.GameEvent;
import com.starbots.starjump.patterns.observer.GameEventListener;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Button;
import com.starbots.starjump.util.Colors;
import com.starbots.starjump.util.Painter;

/**
 * Gameplay. Advances the {@link World} at a fixed 60 Hz, then layers on the
 * animated {@link SpaceBackground}, a pooled {@link ParticleSystem}, screen
 * shake, and player "juice" (squash/stretch, lean, thruster flame). Visual and
 * audio feedback is driven by the {@link EventBus} (Observer) — this state is a
 * listener that turns events into particles + shake.
 */
public final class PlayState implements GameState, GameEventListener {

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;
    private final EventBus bus;
    private final World world;

    private final SpaceBackground background;
    private final ParticleSystem particles;
    private final BreakingPlatformFx breakingFx;
    private final ScreenShake shake = new ScreenShake();
    private final Vector2 tmp = new Vector2();

    private float accumulator;
    private float animTime;
    private boolean deathHandled;
    private float deathTimer;

    // Pause overlay state.
    private boolean paused;
    private boolean goingToSettings;   // suppress the music-stop while popping to Settings
    private boolean runStarted;        // so returning from Settings resumes, not restarts
    private final Button resumeButton;
    private final Button settingsButton;
    private final Button menuButton;

    private static final float PAUSE_BTN_W = 42f;
    private static final float PAUSE_BTN_H = 32f;
    private static final Color PAUSE_DIM = new Color(0f, 0f, 0f, 0.62f);
    private static final Color PAUSE_BTN_BG = new Color(0f, 0f, 0f, 0.45f);

    public PlayState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();
        this.bus = game.bus();
        this.world = new World(game.tilt(), bus);
        this.background = new SpaceBackground(a);
        this.particles = new ParticleSystem(a);
        this.breakingFx = new BreakingPlatformFx(a);

        float bw = 240f, bh = 54f;
        float bx = (Config.WORLD_WIDTH - bw) / 2f;
        float midY = Config.WORLD_HEIGHT / 2f;
        BitmapFont btnFont = a.font(Assets.THALEAH, 24);
        resumeButton = new Button(bx, midY - 80f, bw, bh, "Resume", btnFont,
                Colors.YELLOW_TOP, Colors.YELLOW_BOT);
        settingsButton = new Button(bx, midY - 12f, bw, bh, "Settings", btnFont,
                Colors.GRAY_TOP, Colors.GRAY_BOT);
        menuButton = new Button(bx, midY + 56f, bw, bh, "Menu", btnFont,
                Colors.GRAY_TOP, Colors.GRAY_BOT);
    }

    @Override
    public void enter() {
        // Guard so returning from the pause -> Settings excursion resumes the
        // same run instead of restarting it.
        if (!runStarted) {
            world.startRun();
            accumulator = 0f;
            animTime = 0f;
            deathHandled = false;
            deathTimer = 0f;
            particles.clear();
            breakingFx.clear();
            runStarted = true;
        }
        goingToSettings = false;
        bus.subscribe(this);
    }

    @Override
    public void exit() {
        bus.unsubscribe(this);
        // Keep music going while we pop to Settings; stop it only on a real exit.
        if (!goingToSettings) game.music().stop();
    }

    @Override
    public void update(float delta) {
        if (paused) {
            updatePauseMenu();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)
                || (!deathHandled && pauseButtonClicked())) {
            paused = true;
            return;
        }

        float scrollDy = 0f;
        if (!deathHandled) {
            accumulator += delta;
            while (accumulator >= Config.STEP) {
                world.step();
                scrollDy += world.getLastScrollDy();
                accumulator -= Config.STEP;
                if (world.isGameOver()) break;
            }
            if (world.isGameOver()) {
                ScoreManager.INSTANCE.endRun();   // fires PLAYER_DIED -> our observer
                deathHandled = true;
                deathTimer = 0.7f;                // let the explosion play
            }
        }

        animTime += delta;
        background.update(delta, scrollDy);
        particles.update(delta);
        breakingFx.update(delta);
        shake.update(delta);

        if (deathHandled) {
            deathTimer -= delta;
            if (deathTimer <= 0f) game.gsm().set(new GameOverState(game));
        }
    }

    @Override
    public void onGameEvent(GameEvent event) {
        Player p = world.getPlayer();
        switch (event.type) {
            case PLAYER_JUMPED:
                particles.jumpSparkle(p.x + p.width / 2f, p.y + p.height);
                break;
            case PLATFORM_BROKEN: {
                if (event.payload instanceof Platform) {
                    Platform plat = (Platform) event.payload;
                    breakingFx.shatter(plat.kind, plat.x, plat.y, plat.width, plat.height);
                    particles.debris(plat.x + plat.width / 2f, plat.y + plat.height / 2f);
                }
                break;
            }
            case PLAYER_DIED:
                particles.explosion(p.x + p.width / 2f, p.y + p.height / 2f, 1f, 0.7f, 0.25f, 40);
                shake.shake(18f, 0.5f);
                break;
            case ENEMY_KILLED: {
                Vector2 pos = asPos(event.payload, p);
                particles.explosion(pos.x, pos.y, 0.4f, 0.95f, 1f, 22);
                shake.shake(9f, 0.25f);
                break;
            }
            case BOSS_HIT: {
                Vector2 pos = asPos(event.payload, p);
                particles.explosion(pos.x, pos.y, 1f, 0.45f, 0.45f, 26);
                shake.shake(11f, 0.3f);
                break;
            }
            case BOSS_DEFEATED: {
                Vector2 pos = asPos(event.payload, p);
                particles.explosion(pos.x, pos.y, 1f, 0.85f, 0.3f, 70);
                shake.shake(24f, 0.7f);
                break;
            }
            case SPRING_BOUNCE: {
                Vector2 pos = asPos(event.payload, p);
                particles.springBurst(pos.x, pos.y);
                shake.shake(9f, 0.22f);
                break;
            }
            case LIFE_LOST:
                particles.explosion(p.x + p.width / 2f, p.y + p.height / 2f, 1f, 0.3f, 0.3f, 26);
                shake.shake(14f, 0.4f);
                break;
            case LIFE_GAINED:
                particles.explosion(p.x + p.width / 2f, p.y + p.height / 2f, 0.5f, 1f, 0.6f, 20);
                break;
            case JETPACK_START: {
                Vector2 pos = asPos(event.payload, p);
                particles.explosion(pos.x, pos.y, 1f, 0.7f, 0.2f, 28);
                shake.shake(10f, 0.3f);
                break;
            }
            default:
                break;
        }
    }

    private Vector2 asPos(Object payload, Player fallback) {
        if (payload instanceof Vector2) return (Vector2) payload;
        return tmp.set(fallback.x + fallback.width / 2f, fallback.y + fallback.height / 2f);
    }

    @Override
    public void render(SpriteBatch batch) {
        // Apply screen shake by nudging the camera.
        Camera cam = game.viewport().getCamera();
        cam.position.set(Config.WORLD_WIDTH / 2f + shake.getX(),
                Config.WORLD_HEIGHT / 2f + shake.getY(), 0);
        cam.update();

        Player player = world.getPlayer();
        float safeTop = painter.safeTopInset(); // keep the HUD clear of the notch

        painter.begin();
        background.render(batch);

        for (Platform p : world.getPlatforms()) {
            painter.image(a.platform(p.kind), p.x, p.y, p.width, p.height);
            if (p.hasSpring) {
                drawSpring(p);
            }
        }

        breakingFx.render(batch, Config.WORLD_HEIGHT);
        drawPickups(batch);

        for (Enemy e : world.getEnemies()) {
            painter.image(enemyTexture(e.type), e.x, e.y, e.width, e.height);
        }

        drawBoss(batch);
        drawProjectiles(batch);

        if (!deathHandled) {
            drawThruster(batch, player);
            drawPlayer(batch, player);
        }

        particles.render(batch, Config.WORLD_HEIGHT);

        painter.textRight(a.font(Assets.NASALIZATION, 32),
                String.valueOf(ScoreManager.INSTANCE.getScore()),
                Config.WORLD_WIDTH - 14f, 30f + safeTop);
        drawHud(batch, safeTop);
        drawBossHint(safeTop);
        painter.end();

        // Reset camera to centre so the HUD/overlay draw unshaken.
        cam.position.set(Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT / 2f, 0);
        cam.update();

        if (paused) {
            drawPauseOverlay();
        } else if (!deathHandled) {
            drawPauseButton(safeTop);
        }
    }

    private void drawPickups(SpriteBatch batch) {
        if (world.getPickups().isEmpty()) return;
        float h = Config.WORLD_HEIGHT;
        for (Pickup pk : world.getPickups()) {
            float bob = MathUtils.sin(pk.bobPhase) * 4f;
            float drawY = h - (pk.y + bob) - pk.height;
            // Soft glow halo for visibility.
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            if (pk.type == Pickup.Type.HEART) batch.setColor(1f, 0.4f, 0.5f, 0.5f);
            else                              batch.setColor(1f, 0.7f, 0.3f, 0.5f);
            float g = pk.width * 2f;
            batch.draw(a.glow, pk.centerX() - g / 2f, (h - (pk.y + bob) - pk.height / 2f) - g / 2f, g, g);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            // Sprite.
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(pk.type == Pickup.Type.HEART ? a.heart : a.jetpack,
                    pk.x, drawY, pk.width, pk.height);
        }
    }

    private void drawHud(SpriteBatch batch, float safeTop) {
        // Lives as hearts, top-left, just right of the pause button and below
        // the notch / status region.
        batch.setColor(1f, 1f, 1f, 1f);
        float hx = 12f + PAUSE_BTN_W + 10f, hy = 16f + safeTop, hs = 22f;
        for (int i = 0; i < world.getLives(); i++) {
            painter.image(a.heart, hx + i * (hs + 4f), hy, hs, hs * 0.9f);
        }

        // Jetpack fuel bar (glowy), under the hearts, while flying.
        if (world.isJetpackActive()) {
            float frac = MathUtils.clamp(world.getJetpackTimer() / Config.JETPACK_DURATION, 0f, 1f);
            float bx = 12f, by = 46f + safeTop, bw = 132f, bh = 12f;
            float topY = Config.WORLD_HEIGHT - by - bh;
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.setColor(0.2f, 0.2f, 0.25f, 0.7f);
            batch.draw(a.glow, bx, topY, bw, bh);
            batch.setColor(1f, 0.7f, 0.2f, 0.95f);
            batch.draw(a.glow, bx, topY, bw * frac, bh);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /** Spring sprite, anchored to the platform, with a squash/pop on bounce. */
    private void drawSpring(Platform p) {
        float baseBottom = p.y - 22f + 26f;   // y-down bottom edge, resting on the platform
        float scaleX = 1f, scaleY = 1f;
        if (p.springAnim > 0f) {
            float t = 1f - p.springAnim / World.SPRING_ANIM_TIME; // 0 -> 1 across the bounce
            float pop = MathUtils.sin(t * MathUtils.PI);          // 0..1..0 hump
            scaleY = 1f + 0.8f * pop;                             // springs tall, then settles
            scaleX = 1f - 0.28f * pop;                            // narrows while stretched
        }
        float w = 24f * scaleX;
        float h = 26f * scaleY;
        painter.image(a.spring, p.centerX() - w / 2f, baseBottom - h, w, h);
    }

    /** While a boss is on screen, pulse an instruction — you beat it by bouncing into it. */
    private void drawBossHint(float safeTop) {
        if (world.getBoss() == null) return;
        BitmapFont font = a.font(Assets.NASALIZATION, 16);
        float pulse = 0.55f + 0.45f * MathUtils.sin(animTime * 6f);
        font.setColor(1f, 0.82f, 0.28f, pulse);
        painter.textCentered(font, "JUMP INTO THE BOSS!", Config.WORLD_WIDTH / 2f, 74f + safeTop);
        font.setColor(1f, 1f, 1f, 1f);
    }

    private void drawPlayer(SpriteBatch batch, Player p) {
        float s = p.speed;
        float stretch = MathUtils.clamp(1f + (-s) * 0.010f, 0.82f, 1.2f); // rise=tall, fall=squash
        float scaleY = stretch;
        float scaleX = MathUtils.clamp(2f - stretch, 0.85f, 1.18f);
        float rot = MathUtils.clamp(-game.tilt().getTilt() * 22f, -16f, 16f);

        // Flicker while briefly invulnerable after a hit.
        float alpha = (world.isInvulnerable() && ((int) (animTime * 12f) % 2 == 0)) ? 0.35f : 1f;

        float cx = p.x + p.width / 2f;
        float cyUp = Config.WORLD_HEIGHT - (p.y + p.height / 2f);
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(a.astronaut,
                cx - p.width / 2f, cyUp - p.height / 2f,
                p.width / 2f, p.height / 2f,
                p.width, p.height,
                scaleX, scaleY, rot,
                0, 0, a.astronaut.getWidth(), a.astronaut.getHeight(),
                p.facingLeft, false);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private com.badlogic.gdx.graphics.Texture enemyTexture(EnemyType type) {
        return type == EnemyType.HUNTER ? a.enemyHunter : a.enemyDrone;
    }

    private void drawBoss(SpriteBatch batch) {
        Boss b = world.getBoss();
        if (b == null) return;
        float h = Config.WORLD_HEIGHT;

        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(a.bossSprite, b.x, h - b.y - b.height, b.width, b.height);

        // Hit flash + HP pips (additive).
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        if (b.flash > 0) {
            float gw = b.width * 1.25f, gh = b.height * 1.25f;
            batch.setColor(1f, 0.55f, 0.55f, MathUtils.clamp(b.flash / 0.16f, 0f, 1f) * 0.7f);
            batch.draw(a.glow, b.centerX() - gw / 2f, (h - b.centerY()) - gh / 2f, gw, gh);
        }
        float pip = 14f, gap = 6f;
        float total = b.maxHp * pip + (b.maxHp - 1) * gap;
        float sx = b.centerX() - total / 2f;
        float py = (h - (b.y - 20f)) - pip;
        for (int i = 0; i < b.maxHp; i++) {
            if (i < b.hp) batch.setColor(0.4f, 1f, 0.5f, 0.95f);
            else          batch.setColor(0.35f, 0.35f, 0.35f, 0.6f);
            batch.draw(a.glow, sx + i * (pip + gap), py, pip, pip);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawProjectiles(SpriteBatch batch) {
        if (world.getProjectiles().isEmpty()) return;
        float h = Config.WORLD_HEIGHT;
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        for (Projectile pr : world.getProjectiles()) {
            float cyUp = h - pr.y;
            float outer = pr.radius * 2.8f;
            batch.setColor(1f, 0.45f, 0.2f, 0.85f);
            batch.draw(a.glow, pr.x - outer / 2f, cyUp - outer / 2f, outer, outer);
            float inner = pr.radius * 1.4f;
            batch.setColor(1f, 0.95f, 0.6f, 0.95f);
            batch.draw(a.glow, pr.x - inner / 2f, cyUp - inner / 2f, inner, inner);
        }
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawThruster(SpriteBatch batch, Player p) {
        boolean jet = world.isJetpackActive();
        if (p.speed >= -0.5f && !jet) return; // only while rising / jetpacking
        float fx = p.x + p.width / 2f;
        float feetUp = Config.WORLD_HEIGHT - (p.y + p.height);
        float flick = MathUtils.random(0.75f, 1.15f);
        float boost = jet ? 1.9f : 1f;
        float w = p.width * 0.8f * flick * boost;
        float h = p.height * 0.7f * flick * boost;

        // The jetpack itself, strapped on behind the player.
        if (jet) {
            float jw = 26f, jh = 34f;
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(a.jetpack, fx - jw / 2f, feetUp + 2f, jw, jh);
        }

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 0.6f, 0.15f, 0.85f);
        batch.draw(a.glow, fx - w / 2f, feetUp - h * 0.85f, w, h);
        batch.setColor(1f, 0.95f, 0.5f, 0.95f);
        batch.draw(a.glow, fx - w * 0.3f, feetUp - h * 0.6f, w * 0.6f, h * 0.7f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // --- pause -----------------------------------------------------------------

    private float pauseBtnX() {
        return 12f; // top-left corner, clear of the centre camera / Dynamic Island
    }

    private boolean pauseButtonClicked() {
        return painter.clicked(pauseBtnX(), 12f + painter.safeTopInset(),
                PAUSE_BTN_W, PAUSE_BTN_H);
    }

    private void updatePauseMenu() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || resumeButton.clicked(painter)) {
            paused = false;
            return;
        }
        if (settingsButton.clicked(painter)) {
            goingToSettings = true;                          // exit() keeps the music playing
            game.gsm().set(new SettingsState(game, this));   // returns here when done
            return;
        }
        if (menuButton.clicked(painter)) {
            game.gsm().set(new MenuState(game));
        }
    }

    /** Small pause button, top-left corner, that freezes the run. */
    private void drawPauseButton(float safeTop) {
        float x = pauseBtnX(), top = 12f + safeTop;
        painter.beginShapes();
        painter.rect(x, top, PAUSE_BTN_W, PAUSE_BTN_H, PAUSE_BTN_BG);
        painter.border(x, top, PAUSE_BTN_W, PAUSE_BTN_H, 2f, Colors.WHITE);
        float barW = 6f, gap = 8f, barH = PAUSE_BTN_H - 14f, barTop = top + 7f;
        float cx = x + PAUSE_BTN_W / 2f;
        painter.rect(cx - gap / 2f - barW, barTop, barW, barH, Colors.WHITE);
        painter.rect(cx + gap / 2f, barTop, barW, barH, Colors.WHITE);
        painter.endShapes();
    }

    private void drawPauseOverlay() {
        painter.beginShapes();
        painter.rect(0f, 0f, Config.WORLD_WIDTH, Config.WORLD_HEIGHT, PAUSE_DIM);
        resumeButton.fill(painter);
        settingsButton.fill(painter);
        menuButton.fill(painter);
        painter.endShapes();

        painter.begin();
        BitmapFont title = a.font(Assets.NASALIZATION, 34);
        title.setColor(Colors.WHITE);
        painter.textCentered(title, "Paused",
                Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT / 2f - 150f);
        resumeButton.drawLabel(painter);
        settingsButton.drawLabel(painter);
        menuButton.drawLabel(painter);
        painter.end();
    }

    @Override public void resize(int width, int height) {}
}
