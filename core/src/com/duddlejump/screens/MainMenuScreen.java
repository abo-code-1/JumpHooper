package com.duddlejump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.managers.ScoreManager;

public class MainMenuScreen extends ScreenAdapter {

    private static final int STAR_COUNT = 80;
    private static final int NEBULA_PARTICLES = 20;

    private final DuddleJumpGame game;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont titleFont;
    private final BitmapFont titleShadow;
    private final BitmapFont subtitleFont;
    private final BitmapFont bodyFont;
    private final BitmapFont smallFont;
    private final GlyphLayout layout = new GlyphLayout();

    private Texture dot;
    private Texture glowTex;
    private Texture characterTex;
    private Texture platformTex;
    private Texture nebulaTex;

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];
    private final float[] starR = new float[STAR_COUNT];
    private final float[] starG = new float[STAR_COUNT];
    private final float[] starB = new float[STAR_COUNT];

    private final float[] nebX = new float[NEBULA_PARTICLES];
    private final float[] nebY = new float[NEBULA_PARTICLES];
    private final float[] nebSize = new float[NEBULA_PARTICLES];
    private final float[] nebPhase = new float[NEBULA_PARTICLES];

    private float elapsed = 0f;
    private float characterY;
    private float characterVy;
    private float bounceTimer = 0f;

    public MainMenuScreen(DuddleJumpGame game) {
        this.game = game;
        this.viewport = new FitViewport(
            Config.VIEWPORT_WIDTH,
            Config.VIEWPORT_HEIGHT,
            new OrthographicCamera()
        );
        this.batch = new SpriteBatch();
        this.shapes = new ShapeRenderer();

        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3.2f);
        this.titleShadow = new BitmapFont();
        this.titleShadow.getData().setScale(3.2f);
        this.subtitleFont = new BitmapFont();
        this.subtitleFont.getData().setScale(1.6f);
        this.bodyFont = new BitmapFont();
        this.bodyFont.getData().setScale(1.3f);
        this.smallFont = new BitmapFont();
        this.smallFont.getData().setScale(1f);

        MathUtils.random.setSeed(12345L);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, Config.VIEWPORT_WIDTH);
            starY[i] = MathUtils.random(0f, Config.VIEWPORT_HEIGHT);
            starSize[i] = MathUtils.random(1f, 3.5f);
            starPhase[i] = MathUtils.random(0f, MathUtils.PI2);

            float hue = MathUtils.random();
            if (hue < 0.3f) { starR[i] = 0.92f; starG[i] = 0.72f; starB[i] = 1f; }
            else if (hue < 0.6f) { starR[i] = 0.65f; starG[i] = 0.82f; starB[i] = 1f; }
            else if (hue < 0.8f) { starR[i] = 1f; starG[i] = 0.95f; starB[i] = 0.78f; }
            else { starR[i] = 1f; starG[i] = 0.80f; starB[i] = 0.85f; }
        }

        for (int i = 0; i < NEBULA_PARTICLES; i++) {
            nebX[i] = MathUtils.random(0f, Config.VIEWPORT_WIDTH);
            nebY[i] = MathUtils.random(Config.VIEWPORT_HEIGHT * 0.4f, Config.VIEWPORT_HEIGHT);
            nebSize[i] = MathUtils.random(40f, 100f);
            nebPhase[i] = MathUtils.random(MathUtils.PI2);
        }

        characterY = Config.VIEWPORT_HEIGHT * 0.38f;
        characterVy = 0f;
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        if (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER)
            || Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
            return;
        }

        updateCharacterBounce(delta);

        // Draw gradient background
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Color top = new Color(0.04f, 0.02f, 0.12f, 1f);
        Color bot = new Color(0.16f, 0.06f, 0.30f, 1f);
        shapes.rect(0, 0, Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT, bot, bot, top, top);
        shapes.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        ensureTextures();
        renderNebula();
        renderStars();
        renderCharacterBouncing();
        renderTitle();
        renderUI();

        batch.end();
    }

    private void updateCharacterBounce(float delta) {
        characterVy += -600f * delta;
        characterY += characterVy * delta;

        float platformY = Config.VIEWPORT_HEIGHT * 0.30f;
        if (characterY <= platformY + 18f && characterVy < 0f) {
            characterVy = 350f;
            characterY = platformY + 18f;
            bounceTimer = 0.15f;
        }
        bounceTimer = Math.max(0f, bounceTimer - delta);
    }

    private void renderNebula() {
        for (int i = 0; i < NEBULA_PARTICLES; i++) {
            float pulse = 0.4f + 0.3f * MathUtils.sin(elapsed * 0.8f + nebPhase[i]);
            float nr, ng, nb;
            if (i % 3 == 0) { nr = 0.5f; ng = 0.15f; nb = 0.7f; }
            else if (i % 3 == 1) { nr = 0.15f; ng = 0.35f; nb = 0.7f; }
            else { nr = 0.6f; ng = 0.20f; nb = 0.45f; }

            batch.setColor(nr, ng, nb, 0.04f * pulse);
            float s = nebSize[i];
            batch.draw(nebulaTex, nebX[i] - s * 0.5f, nebY[i] - s * 0.5f, s, s);
        }
    }

    private void renderStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            float twinkle = 0.3f + 0.7f * (0.5f + 0.5f * MathUtils.sin(elapsed * 3f + starPhase[i]));
            float s = starSize[i];

            batch.setColor(starR[i], starG[i], starB[i], twinkle * 0.75f);
            batch.draw(dot, starX[i], starY[i], s, s);

            // Glow for bigger stars
            if (s > 2.5f) {
                batch.setColor(starR[i], starG[i], starB[i], twinkle * 0.08f);
                float gs = s * 4f;
                batch.draw(glowTex, starX[i] - gs * 0.4f, starY[i] - gs * 0.4f, gs, gs);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void renderCharacterBouncing() {
        float platformX = (Config.VIEWPORT_WIDTH - 72f) * 0.5f;
        float platformY = Config.VIEWPORT_HEIGHT * 0.30f;

        // Platform glow
        batch.setColor(0.22f, 0.65f, 0.28f, 0.12f);
        float platGlow = 90f;
        batch.draw(glowTex, platformX - 9f, platformY - 5f, platGlow, platGlow * 0.4f);

        // Platform
        batch.setColor(Color.WHITE);
        batch.draw(platformTex, platformX, platformY, 72f, 18f);

        // Character glow
        float charCx = Config.VIEWPORT_WIDTH * 0.5f;
        float glowPulse = 0.15f + 0.1f * MathUtils.sin(elapsed * 3f);
        batch.setColor(0.15f, 0.80f, 0.78f, glowPulse);
        float charGlow = 50f;
        batch.draw(glowTex, charCx - charGlow * 0.5f, characterY - charGlow * 0.3f, charGlow, charGlow);

        // Character with squash/stretch
        float charX = (Config.VIEWPORT_WIDTH - 40f) * 0.5f;
        float scaleY = 1f + (bounceTimer > 0f ? bounceTimer * 3f : 0f);
        float scaleX = 1f / scaleY;
        float drawW = 40f * scaleX;
        float drawH = 48f * scaleY;
        float offsetX = (40f - drawW) * 0.5f;

        batch.setColor(Color.WHITE);
        batch.draw(characterTex, charX + offsetX, characterY, drawW, drawH);
    }

    private void renderTitle() {
        float titleY = Config.VIEWPORT_HEIGHT * 0.82f;

        // Title shadow
        titleShadow.setColor(new Color(0f, 0f, 0f, 0.4f));
        drawCentered(titleShadow, "JUMPHOOPER", titleY - 3f);

        // Title outer glow
        float glowPulse = 0.4f + 0.4f * MathUtils.sin(elapsed * 2f);
        titleFont.setColor(new Color(0.15f, 0.85f, 0.82f, 0.08f * glowPulse));
        drawCentered(titleFont, "JUMPHOOPER", titleY + 3f);
        drawCentered(titleFont, "JUMPHOOPER", titleY - 1f);

        // Title main
        titleFont.setColor(new Color(0.18f, 0.95f, 0.92f, 1f));
        drawCentered(titleFont, "JUMPHOOPER", titleY);

        // Subtitle
        subtitleFont.setColor(new Color(0.60f, 0.50f, 0.82f, 0.75f));
        drawCentered(subtitleFont, "jump to the cosmos", Config.VIEWPORT_HEIGHT * 0.72f);
    }

    private void renderUI() {
        // Play prompt - pulsing with glow
        float pulse = 0.55f + 0.45f * MathUtils.sin(elapsed * 3f);
        bodyFont.setColor(new Color(1f, 0.72f, 0.25f, pulse));
        drawCentered(bodyFont, "press SPACE or tap to play", Config.VIEWPORT_HEIGHT * 0.18f);

        // High score with icon
        int high = ScoreManager.INSTANCE.getHigh();
        if (high > 0) {
            bodyFont.setColor(new Color(0.78f, 0.70f, 0.95f, 0.82f));
            drawCentered(bodyFont, "best score: " + high, Config.VIEWPORT_HEIGHT * 0.12f);
        }

        // Controls
        smallFont.setColor(new Color(0.42f, 0.38f, 0.52f, 0.55f));
        drawCentered(smallFont, "A/D or arrows to move  |  P to pause", Config.VIEWPORT_HEIGHT * 0.05f);
    }

    private void ensureTextures() {
        if (dot != null) return;

        // Dot
        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fillCircle(2, 2, 2);
        dot = new Texture(pix);
        pix.dispose();

        // Glow
        int gs = 32;
        Pixmap gp = new Pixmap(gs, gs, Pixmap.Format.RGBA8888);
        gp.setColor(0, 0, 0, 0);
        gp.fill();
        for (int r = gs / 2; r > 0; r--) {
            float t = 1f - (float) r / (gs / 2);
            gp.setColor(new Color(1f, 1f, 1f, t * t));
            gp.fillCircle(gs / 2, gs / 2, r);
        }
        glowTex = new Texture(gp);
        gp.dispose();

        // Nebula texture
        int ns = 64;
        Pixmap np = new Pixmap(ns, ns, Pixmap.Format.RGBA8888);
        np.setColor(0, 0, 0, 0);
        np.fill();
        for (int i = 0; i < 8; i++) {
            int cx = ns / 4 + (i * 11) % (ns / 2);
            int cy = ns / 4 + (i * 13) % (ns / 2);
            int r = 8 + (i * 5) % 12;
            np.setColor(new Color(0.8f, 0.5f, 1f, 0.15f));
            np.fillCircle(cx, cy, r);
        }
        nebulaTex = new Texture(np);
        np.dispose();

        characterTex = buildMenuCharacter();
        platformTex = buildMenuPlatform();
    }

    private static Texture buildMenuCharacter() {
        int w = 40, h = 48;
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();

        int cx = w / 2, cy = h / 2 + 4;

        // Shadow
        p.setColor(new Color(0.05f, 0.28f, 0.30f, 0.4f));
        p.fillCircle(cx + 1, cy + 2, 15);

        // Body outer
        p.setColor(new Color(0.10f, 0.62f, 0.65f, 1f));
        p.fillCircle(cx, cy, 15);

        // Body
        p.setColor(new Color(0.15f, 0.78f, 0.78f, 1f));
        p.fillCircle(cx, cy, 14);

        // Highlight
        p.setColor(new Color(0.28f, 0.92f, 0.88f, 1f));
        p.fillCircle(cx - 3, cy - 4, 10);

        // Specular
        p.setColor(new Color(0.50f, 0.97f, 0.95f, 0.5f));
        p.fillCircle(cx - 5, cy - 6, 5);

        // Belly
        p.setColor(new Color(0.55f, 0.95f, 0.92f, 0.45f));
        p.fillCircle(cx, cy + 4, 8);

        // Eyes
        p.setColor(Color.WHITE);
        p.fillCircle(cx - 5, cy - 3, 5);
        p.fillCircle(cx + 5, cy - 3, 5);
        p.setColor(new Color(0.05f, 0.05f, 0.10f, 1f));
        p.fillCircle(cx - 5, cy - 4, 3);
        p.fillCircle(cx + 5, cy - 4, 3);
        p.setColor(Color.WHITE);
        p.fillCircle(cx - 4, cy - 5, 1);
        p.fillCircle(cx + 6, cy - 5, 1);

        // Cheeks
        p.setColor(new Color(1f, 0.55f, 0.50f, 0.25f));
        p.fillCircle(cx - 10, cy, 4);
        p.fillCircle(cx + 10, cy, 4);

        // Smile
        p.setColor(new Color(0.06f, 0.30f, 0.33f, 1f));
        p.drawLine(cx - 4, cy + 4, cx - 1, cy + 6);
        p.drawLine(cx - 1, cy + 6, cx + 1, cy + 6);
        p.drawLine(cx + 1, cy + 6, cx + 4, cy + 4);

        // Antennae
        p.setColor(new Color(0.08f, 0.48f, 0.52f, 1f));
        for (int t = -1; t <= 0; t++) {
            p.drawLine(cx - 4 + t, cy - 14, cx - 7 + t, cy - 21);
            p.drawLine(cx + 4 + t, cy - 14, cx + 7 + t, cy - 21);
        }
        // Antenna tips
        p.setColor(new Color(1f, 0.55f, 0.10f, 0.4f));
        p.fillCircle(cx - 7, cy - 21, 4);
        p.fillCircle(cx + 7, cy - 21, 4);
        p.setColor(new Color(1f, 0.65f, 0.15f, 1f));
        p.fillCircle(cx - 7, cy - 21, 3);
        p.fillCircle(cx + 7, cy - 21, 3);
        p.setColor(new Color(1f, 0.85f, 0.40f, 0.8f));
        p.fillCircle(cx - 7, cy - 21, 1);
        p.fillCircle(cx + 7, cy - 21, 1);

        // Feet
        p.setColor(new Color(0.08f, 0.50f, 0.52f, 1f));
        p.fillCircle(cx - 6, cy + 15, 4);
        p.fillCircle(cx + 6, cy + 15, 4);
        p.setColor(new Color(0.15f, 0.65f, 0.65f, 1f));
        p.fillCircle(cx - 6, cy + 14, 3);
        p.fillCircle(cx + 6, cy + 14, 3);

        // Outline
        p.setColor(new Color(0.04f, 0.38f, 0.40f, 0.5f));
        p.drawCircle(cx, cy, 14);

        Texture tex = new Texture(p);
        p.dispose();
        return tex;
    }

    private static Texture buildMenuPlatform() {
        int w = 72, h = 18;
        Pixmap px = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        px.setColor(0, 0, 0, 0);
        px.fill();

        int r = 6;

        // Shadow
        px.setColor(new Color(0.08f, 0.30f, 0.10f, 0.4f));
        fillRoundRect(px, 3, 4, w - 6, h - 3, r);

        // Dark bottom
        px.setColor(new Color(0.16f, 0.48f, 0.20f, 1f));
        fillRoundRect(px, 1, 2, w - 2, h - 3, r);

        // Main body
        px.setColor(new Color(0.22f, 0.65f, 0.28f, 1f));
        fillRoundRect(px, 1, 1, w - 2, h - 4, r);

        // Highlight
        px.setColor(new Color(0.35f, 0.82f, 0.40f, 1f));
        fillRoundRect(px, 3, 1, w - 6, h / 2, r - 1);

        // Shine
        px.setColor(new Color(0.55f, 0.92f, 0.55f, 0.45f));
        fillRoundRect(px, 8, 2, w - 16, 3, 2);

        // Grass
        px.setColor(new Color(0.30f, 0.78f, 0.35f, 1f));
        for (int i = 6; i < w - 6; i += 6) {
            px.fillRectangle(i, 0, 2, 3 + (i % 3));
        }

        Texture tex = new Texture(px);
        px.dispose();
        return tex;
    }

    private static void fillRoundRect(Pixmap px, int x, int y, int w, int h, int r) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        px.fillRectangle(x + r, y, w - 2 * r, h);
        px.fillRectangle(x, y + r, w, h - 2 * r);
        px.fillCircle(x + r, y + r, r);
        px.fillCircle(x + w - r - 1, y + r, r);
        px.fillCircle(x + r, y + h - r - 1, r);
        px.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        titleFont.dispose();
        titleShadow.dispose();
        subtitleFont.dispose();
        bodyFont.dispose();
        smallFont.dispose();
        if (dot != null) dot.dispose();
        if (glowTex != null) glowTex.dispose();
        if (characterTex != null) characterTex.dispose();
        if (platformTex != null) platformTex.dispose();
        if (nebulaTex != null) nebulaTex.dispose();
    }

    private void drawCentered(BitmapFont font, String text, float y) {
        layout.setText(font, text);
        float x = (Config.VIEWPORT_WIDTH - layout.width) * 0.5f;
        font.draw(batch, layout, x, y);
    }
}
