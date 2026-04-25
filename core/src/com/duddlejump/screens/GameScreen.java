package com.duddlejump.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.background.BackgroundManager;
import com.duddlejump.entities.Doodle;
import com.duddlejump.entities.ParticlePool;
import com.duddlejump.entities.Platform;
import com.duddlejump.entities.PlatformKind;
import com.duddlejump.entities.PlatformSpawner;
import com.duddlejump.events.EventBus;
import com.duddlejump.factories.PlatformFactory;
import com.duddlejump.input.InputController;
import com.duddlejump.input.KeyboardInputController;
import com.duddlejump.managers.ScoreManager;
import com.duddlejump.state.GameOverState;
import com.duddlejump.state.GameState;
import com.duddlejump.state.PausedState;
import com.duddlejump.state.PlayingState;

public class GameScreen extends ScreenAdapter {

    private final DuddleJumpGame game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final InputController input;
    private final EventBus bus;
    private final PlatformFactory platformFactory;
    private final PlatformSpawner spawner;
    private final Doodle doodle;
    private final BackgroundManager background;
    private final ParticlePool particles;

    // HUD
    private final BitmapFont scoreFont;
    private final BitmapFont scoreShadowFont;
    private final BitmapFont biomeFont;
    private final BitmapFont biomeShadowFont;
    private final BitmapFont smallFont;
    private final GlyphLayout hudLayout = new GlyphLayout();

    // Vignette overlay
    private Texture vignetteTex;

    private final PlayingState playingState;
    private final PausedState pausedState;
    private final GameOverState gameOverState;
    private GameState current;

    private float highestY;
    private float scoreBaselineY;
    private boolean gameOverQueued;

    private String lastBiomeName = "";
    private float biomeBannerAlpha = 0f;
    private float biomeBannerTimer = 0f;

    // Screen shake
    private float shakeIntensity = 0f;
    private float shakeTimer = 0f;

    // Score pop animation
    private float scoreScale = 1f;
    private int lastDisplayedScore = 0;

    private float elapsed = 0f;

    public GameScreen(DuddleJumpGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT, camera);
        this.batch = new SpriteBatch();
        this.input = new KeyboardInputController();
        this.bus = new EventBus();
        this.platformFactory = new PlatformFactory();
        this.spawner = new PlatformSpawner(platformFactory);
        this.background = new BackgroundManager();
        this.particles = new ParticlePool();

        this.scoreFont = new BitmapFont();
        this.scoreFont.getData().setScale(2.0f);
        this.scoreShadowFont = new BitmapFont();
        this.scoreShadowFont.getData().setScale(2.0f);
        this.biomeFont = new BitmapFont();
        this.biomeFont.getData().setScale(1.6f);
        this.biomeShadowFont = new BitmapFont();
        this.biomeShadowFont.getData().setScale(1.6f);
        this.smallFont = new BitmapFont();
        this.smallFont.getData().setScale(1f);

        Texture sprite = createPlaceholderSprite();
        float startX = (Config.VIEWPORT_WIDTH - Doodle.WIDTH) * 0.5f;
        float startY = Config.VIEWPORT_HEIGHT * 0.25f;
        this.doodle = new Doodle(sprite, startX, startY, true);
        this.doodle.bounce();
        this.highestY = startY;
        this.scoreBaselineY = startY;

        camera.position.set(Config.VIEWPORT_WIDTH * 0.5f, Config.VIEWPORT_HEIGHT * 0.5f, 0f);
        camera.update();

        spawner.seed(startY - 40f, camera.position.y + Config.VIEWPORT_HEIGHT);

        // Observer pattern — subscribers react to contact events
        bus.subscribe((d, p) -> {
            p.onContact(d);
            emitContactParticles(p);
            triggerBounceShake(p.getKind());
        });

        this.playingState = new PlayingState(this);
        this.pausedState = new PausedState(this);
        this.gameOverState = new GameOverState(this);
        this.current = playingState;
        this.current.enter();
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        current.update(delta);
        particles.update(delta);
        background.update(highestY - scoreBaselineY);
        updateBiomeBanner(delta);
        updateShake(delta);
        updateScoreAnim(delta);

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();

        // Apply screen shake
        float savedCamX = camera.position.x;
        float savedCamY = camera.position.y;
        if (shakeTimer > 0f) {
            camera.position.x += MathUtils.random(-shakeIntensity, shakeIntensity);
            camera.position.y += MathUtils.random(-shakeIntensity, shakeIntensity);
            camera.update();
        }

        background.renderGradient(camera, Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        background.renderDecorations(batch, camera, Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);
        current.render(batch);
        particles.render(batch);
        renderHUD();
        renderVignette();
        batch.end();

        // Restore camera
        if (shakeTimer > 0f) {
            camera.position.x = savedCamX;
            camera.position.y = savedCamY;
            camera.update();
        }
    }

    public void updateWorld(float dt) {
        doodle.update(dt, input);

        float cameraTop = camera.position.y + Config.VIEWPORT_HEIGHT * 0.5f;
        float cameraBottom = camera.position.y - Config.VIEWPORT_HEIGHT * 0.5f;

        spawner.update(dt, cameraBottom, cameraTop);
        doodle.checkContact(spawner.getPlatforms(), bus);

        if (doodle.getPosition().y > camera.position.y) {
            camera.position.y = doodle.getPosition().y;
            camera.update();
        }
        if (doodle.getPosition().y > highestY) {
            highestY = doodle.getPosition().y;
            ScoreManager.INSTANCE.setCurrent((int) ((highestY - scoreBaselineY) / 10f));
        }
    }

    private void triggerBounceShake(PlatformKind kind) {
        switch (kind) {
            case SPRING:
                shakeIntensity = 4f;
                shakeTimer = 0.15f;
                break;
            case RED:
                shakeIntensity = 3f;
                shakeTimer = 0.12f;
                break;
            default:
                shakeIntensity = 1.5f;
                shakeTimer = 0.06f;
                break;
        }
    }

    private void updateShake(float delta) {
        if (shakeTimer > 0f) {
            shakeTimer -= delta;
            shakeIntensity *= 0.9f;
        }
    }

    private void updateScoreAnim(float delta) {
        int currentScore = getScore();
        if (currentScore != lastDisplayedScore) {
            // Pop when score changes by significant amount
            if (currentScore - lastDisplayedScore >= 5) {
                scoreScale = 1.3f;
            }
            lastDisplayedScore = currentScore;
        }
        scoreScale += (1f - scoreScale) * 8f * delta;
    }

    private void emitContactParticles(Platform platform) {
        float px = platform.getBounds().x + platform.getBounds().width * 0.5f;
        float py = platform.getBounds().y + platform.getBounds().height;

        PlatformKind kind = platform.getKind();
        switch (kind) {
            case GREEN:
                particles.emitUpward(px, py, 8, 140f, 55f, 4.5f,
                    0.35f, 0.85f, 0.40f, 0.5f);
                particles.emitBurst(px, py, 4, 80f, 3f,
                    0.55f, 0.95f, 0.55f, 0.3f);
                break;
            case RED:
                particles.emitRadial(px, py, 16, 200f, 5.5f,
                    0.95f, 0.32f, 0.22f, true, 0.7f);
                particles.emitFalling(px, py, 8, 90f, 70f, 3.5f,
                    0.60f, 0.18f, 0.12f, 0.6f);
                particles.emitBurst(px, py, 6, 120f, 4f,
                    1f, 0.60f, 0.25f, 0.4f);
                break;
            case BLUE:
                particles.emitUpward(px, py, 10, 110f, 45f, 3.5f,
                    0.50f, 0.78f, 1f, 0.6f);
                particles.emitBurst(px, py, 5, 90f, 3f,
                    0.80f, 0.92f, 1f, 0.4f);
                break;
            case WHITE:
                particles.emitRising(px, py, 12, 65f, 3.5f,
                    0.95f, 0.95f, 1f, 0.9f);
                particles.emitBurst(px, py, 4, 40f, 2.5f,
                    1f, 1f, 1f, 0.6f);
                break;
            case SPRING:
                particles.emitRadial(px, py, 18, 220f, 4.5f,
                    1f, 0.82f, 0.28f, true, 0.6f);
                particles.emitUpward(px, py + 12, 8, 180f, 35f, 3.5f,
                    0.88f, 0.72f, 0.22f, 0.4f);
                particles.emitBurst(px, py, 8, 150f, 5f,
                    1f, 0.95f, 0.50f, 0.5f);
                break;
        }
    }

    private void renderHUD() {
        float camTop = camera.position.y + Config.VIEWPORT_HEIGHT * 0.5f;
        float camLeft = camera.position.x - Config.VIEWPORT_WIDTH * 0.5f;

        // Score with shadow and scale animation
        int score = getScore();
        String scoreStr = String.valueOf(score);

        float sx = camLeft + 16f;
        float sy = camTop - 14f;

        // Score shadow
        scoreShadowFont.getData().setScale(2.0f * scoreScale);
        scoreShadowFont.setColor(new Color(0f, 0f, 0f, 0.4f));
        scoreShadowFont.draw(batch, scoreStr, sx + 2f, sy - 2f);

        // Score glow (subtle)
        scoreFont.getData().setScale(2.0f * scoreScale);
        scoreFont.setColor(new Color(0.2f, 0.9f, 0.9f, 0.15f));
        scoreFont.draw(batch, scoreStr, sx - 1f, sy + 1f);

        // Score main
        scoreFont.setColor(new Color(1f, 1f, 1f, 0.92f));
        scoreFont.draw(batch, scoreStr, sx, sy);

        // Reset font scale
        scoreFont.getData().setScale(2.0f);
        scoreShadowFont.getData().setScale(2.0f);

        // Biome banner with enhanced styling
        if (biomeBannerAlpha > 0.01f) {
            String bannerText = lastBiomeName.toUpperCase();
            hudLayout.setText(biomeFont, bannerText);
            float bx = camLeft + (Config.VIEWPORT_WIDTH - hudLayout.width) * 0.5f;
            float by = camTop - Config.VIEWPORT_HEIGHT * 0.07f;

            // Banner background pill
            float pillW = hudLayout.width + 40f;
            float pillH = 30f;
            float pillX = bx - 20f;
            float pillY = by - 22f;

            // Use dot texture as background if we have it
            batch.setColor(0f, 0f, 0f, biomeBannerAlpha * 0.25f);
            // Just use a simple colored draw
            // (We can't easily draw a rounded rect in SpriteBatch, so we'll just do the text)

            // Shadow
            biomeShadowFont.setColor(new Color(0f, 0f, 0f, biomeBannerAlpha * 0.5f));
            biomeShadowFont.draw(batch, bannerText, bx + 2f, by - 2f);

            // Glow
            biomeFont.setColor(new Color(0.3f, 0.9f, 1f, biomeBannerAlpha * 0.2f));
            biomeFont.draw(batch, bannerText, bx - 1f, by + 1f);

            // Main text
            biomeFont.setColor(new Color(1f, 1f, 1f, biomeBannerAlpha * 0.85f));
            biomeFont.draw(batch, hudLayout, bx, by);

            batch.setColor(Color.WHITE);
        }
    }

    private void renderVignette() {
        if (vignetteTex == null) {
            vignetteTex = buildVignette();
        }
        float camLeft = camera.position.x - Config.VIEWPORT_WIDTH * 0.5f;
        float camBot = camera.position.y - Config.VIEWPORT_HEIGHT * 0.5f;
        batch.setColor(1f, 1f, 1f, 0.35f);
        batch.draw(vignetteTex, camLeft, camBot, Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT);
        batch.setColor(Color.WHITE);
    }

    private static Texture buildVignette() {
        int w = 240, h = 400;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        float cx = w * 0.5f, cy = h * 0.5f;
        float maxDist = (float) Math.sqrt(cx * cx + cy * cy);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float dx = (x - cx) / cx;
                float dy = (y - cy) / cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float vignette = Math.max(0f, dist - 0.5f) * 1.5f;
                vignette = Math.min(vignette, 1f);
                vignette = vignette * vignette;
                if (vignette > 0.01f) {
                    px.setColor(new Color(0f, 0f, 0f, vignette * 0.6f));
                    px.drawPixel(x, y);
                }
            }
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private void updateBiomeBanner(float delta) {
        String currentName = background.getBannerLabel();
        if (!currentName.equals(lastBiomeName)) {
            lastBiomeName = currentName;
            biomeBannerAlpha = 1f;
            biomeBannerTimer = 2.5f;
        }
        if (biomeBannerTimer > 0f) {
            biomeBannerTimer -= delta;
            if (biomeBannerTimer <= 0.5f) {
                biomeBannerAlpha = Math.max(0f, biomeBannerTimer * 2f);
            }
        } else {
            biomeBannerAlpha = 0f;
        }
    }

    public DuddleJumpGame getGame() {
        return game;
    }

    public int getScore() {
        return (int) ((highestY - scoreBaselineY) / 10f);
    }

    public void setState(GameState next) {
        if (current != null) {
            current.exit();
        }
        current = next;
        current.enter();
    }

    public boolean isDoodleBelowCamera() {
        return doodle.getPosition().y + Doodle.HEIGHT < camera.position.y - Config.VIEWPORT_HEIGHT * 0.5f;
    }

    public void queueGameOver() {
        gameOverQueued = true;
    }

    public boolean isGameOverQueued() {
        return gameOverQueued;
    }

    public InputController getInput() {
        return input;
    }

    public Doodle getDoodle() {
        return doodle;
    }

    public PlatformSpawner getSpawner() {
        return spawner;
    }

    public EventBus getBus() {
        return bus;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public float getHighestY() {
        return highestY;
    }

    public PlayingState getPlayingState() {
        return playingState;
    }

    public PausedState getPausedState() {
        return pausedState;
    }

    public GameOverState getGameOverState() {
        return gameOverState;
    }

    public GameState getCurrentState() {
        return current;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        doodle.dispose();
        platformFactory.dispose();
        gameOverState.dispose();
        pausedState.dispose();
        particles.dispose();
        background.dispose();
        scoreFont.dispose();
        scoreShadowFont.dispose();
        biomeFont.dispose();
        biomeShadowFont.dispose();
        smallFont.dispose();
        batch.dispose();
        if (vignetteTex != null) vignetteTex.dispose();
    }

    private static Texture createPlaceholderSprite() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
