package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.fx.ParticleSystem;
import com.starbots.starjump.fx.ScreenShake;
import com.starbots.starjump.fx.SpaceBackground;
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
    private final ScreenShake shake = new ScreenShake();
    private final Vector2 tmp = new Vector2();

    private float accumulator;
    private boolean deathHandled;
    private float deathTimer;

    public PlayState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();
        this.bus = game.bus();
        this.world = new World(game.tilt(), bus);
        this.background = new SpaceBackground(a);
        this.particles = new ParticleSystem(a);
    }

    @Override
    public void enter() {
        world.startRun();
        accumulator = 0f;
        deathHandled = false;
        deathTimer = 0f;
        particles.clear();
        bus.subscribe(this);
    }

    @Override
    public void exit() {
        bus.unsubscribe(this);
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            game.gsm().set(new MenuState(game));
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

        background.update(delta, scrollDy);
        particles.update(delta);
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
                Vector2 pos = asPos(event.payload, p);
                particles.debris(pos.x, pos.y);
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

        painter.begin();
        background.render(batch);

        for (Platform p : world.getPlatforms()) {
            painter.image(a.platform(p.kind), p.x, p.y, p.width, p.height);
        }

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

        painter.textCentered(a.font(Assets.NASALIZATION, 32),
                String.valueOf(ScoreManager.INSTANCE.getScore()),
                Config.WORLD_WIDTH / 2f, 30f);
        painter.end();

        // Reset camera to centre.
        cam.position.set(Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT / 2f, 0);
        cam.update();
    }

    private void drawPlayer(SpriteBatch batch, Player p) {
        float s = p.speed;
        float stretch = MathUtils.clamp(1f + (-s) * 0.010f, 0.82f, 1.2f); // rise=tall, fall=squash
        float scaleY = stretch;
        float scaleX = MathUtils.clamp(2f - stretch, 0.85f, 1.18f);
        float rot = MathUtils.clamp(-game.tilt().getTilt() * 22f, -16f, 16f);

        float cx = p.x + p.width / 2f;
        float cyUp = Config.WORLD_HEIGHT - (p.y + p.height / 2f);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(a.astronaut,
                cx - p.width / 2f, cyUp - p.height / 2f,
                p.width / 2f, p.height / 2f,
                p.width, p.height,
                scaleX, scaleY, rot,
                0, 0, a.astronaut.getWidth(), a.astronaut.getHeight(),
                p.facingLeft, false);
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
        if (p.speed >= -0.5f) return; // only while rising
        float fx = p.x + p.width / 2f;
        float feetUp = Config.WORLD_HEIGHT - (p.y + p.height);
        float flick = MathUtils.random(0.75f, 1.15f);
        float w = p.width * 0.8f * flick;
        float h = p.height * 0.7f * flick;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 0.6f, 0.15f, 0.8f);
        batch.draw(a.glow, fx - w / 2f, feetUp - h * 0.85f, w, h);
        batch.setColor(1f, 0.95f, 0.5f, 0.9f);
        batch.draw(a.glow, fx - w * 0.3f, feetUp - h * 0.6f, w * 0.6f, h * 0.7f);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override public void resize(int width, int height) {}
}
