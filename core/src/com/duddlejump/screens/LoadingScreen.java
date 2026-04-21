package com.duddlejump.screens;

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
import com.duddlejump.managers.Assets;

public class LoadingScreen extends ScreenAdapter {

    private static final float MIN_DURATION = 0.8f;
    private static final int STAR_COUNT = 40;

    private final DuddleJumpGame game;
    private final Assets assets;
    private final Viewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapes;
    private final BitmapFont titleFont;
    private final BitmapFont titleShadow;
    private final BitmapFont bodyFont;
    private final GlyphLayout layout = new GlyphLayout();

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starSize = new float[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];

    private Texture dot;
    private Texture characterTex;
    private Texture glowTex;
    private float elapsed;
    private float characterY;
    private float characterVy = 200f;

    public LoadingScreen(DuddleJumpGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.viewport = new FitViewport(
            Config.VIEWPORT_WIDTH,
            Config.VIEWPORT_HEIGHT,
            new OrthographicCamera()
        );
        this.batch = new SpriteBatch();
        this.shapes = new ShapeRenderer();
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(2.2f);
        this.titleShadow = new BitmapFont();
        this.titleShadow.getData().setScale(2.2f);
        this.bodyFont = new BitmapFont();
        this.bodyFont.getData().setScale(1.2f);

        this.characterY = Config.VIEWPORT_HEIGHT * 0.5f;

        MathUtils.random.setSeed(7777L);
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, Config.VIEWPORT_WIDTH);
            starY[i] = MathUtils.random(0f, Config.VIEWPORT_HEIGHT);
            starSize[i] = MathUtils.random(1f, 2.5f);
            starPhase[i] = MathUtils.random(MathUtils.PI2);
        }
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        boolean loaded = assets.update();
        float progress = assets.progress();

        characterVy += -500f * delta;
        characterY += characterVy * delta;
        if (characterY < Config.VIEWPORT_HEIGHT * 0.42f) {
            characterY = Config.VIEWPORT_HEIGHT * 0.42f;
            characterVy = 250f;
        }

        // Gradient background
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Color top = new Color(0.04f, 0.02f, 0.12f, 1f);
        Color bot = new Color(0.12f, 0.06f, 0.25f, 1f);
        shapes.rect(0, 0, Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT, bot, bot, top, top);
        shapes.end();

        // Progress bar with glow
        float barW = 280f;
        float barH = 14f;
        float barX = (Config.VIEWPORT_WIDTH - barW) * 0.5f;
        float barY = Config.VIEWPORT_HEIGHT * 0.35f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Bar outer glow
        shapes.setColor(0.10f, 0.55f, 0.55f, 0.15f);
        shapes.rect(barX - 4, barY - 4, barW + 8, barH + 8);
        // Bar background
        shapes.setColor(0.10f, 0.10f, 0.20f, 1f);
        shapes.rect(barX - 1, barY - 1, barW + 2, barH + 2);
        // Bar fill gradient
        float fillW = barW * progress;
        shapes.rect(barX, barY, fillW, barH,
            new Color(0.10f, 0.60f, 0.62f, 1f), new Color(0.18f, 0.82f, 0.80f, 1f),
            new Color(0.18f, 0.82f, 0.80f, 1f), new Color(0.10f, 0.60f, 0.62f, 1f));
        // Bar highlight
        shapes.setColor(0.35f, 0.95f, 0.92f, 0.45f);
        shapes.rect(barX, barY + barH * 0.65f, fillW, barH * 0.25f);
        // Bar tip glow
        if (progress > 0.01f) {
            shapes.setColor(0.40f, 1f, 0.95f, 0.3f);
            shapes.rect(barX + fillW - 3, barY - 2, 6, barH + 4);
        }
        shapes.end();

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        ensureTextures();

        // Background stars
        for (int i = 0; i < STAR_COUNT; i++) {
            float twinkle = 0.3f + 0.7f * (0.5f + 0.5f * MathUtils.sin(elapsed * 2.5f + starPhase[i]));
            batch.setColor(0.7f, 0.75f, 1f, twinkle * 0.5f);
            batch.draw(dot, starX[i], starY[i], starSize[i], starSize[i]);
        }

        // Character glow
        float glowPulse = 0.3f + 0.15f * MathUtils.sin(elapsed * 3f);
        batch.setColor(0.15f, 0.8f, 0.8f, glowPulse);
        float glowS = 60f;
        float charCx = Config.VIEWPORT_WIDTH * 0.5f;
        batch.draw(glowTex, charCx - glowS, characterY - glowS * 0.5f, glowS * 2f, glowS * 2f);

        // Bouncing character
        float squash = characterVy > 0f ? 1.15f : 0.9f;
        float invSquash = 1f / squash;
        float drawW = 36f * invSquash;
        float drawH = 44f * squash;
        float charX = (Config.VIEWPORT_WIDTH - drawW) * 0.5f;
        batch.setColor(Color.WHITE);
        batch.draw(characterTex, charX, characterY, drawW, drawH);

        // Title shadow
        titleShadow.setColor(new Color(0f, 0f, 0f, 0.4f));
        drawCentered(titleShadow, "JUMPHOOPER", Config.VIEWPORT_HEIGHT * 0.72f - 2f);

        // Title glow
        float titleGlow = 0.5f + 0.3f * MathUtils.sin(elapsed * 2f);
        titleFont.setColor(new Color(0.15f, 0.85f, 0.85f, 0.12f * titleGlow));
        drawCentered(titleFont, "JUMPHOOPER", Config.VIEWPORT_HEIGHT * 0.72f + 2f);

        // Title main
        titleFont.setColor(new Color(0.20f, 0.95f, 0.92f, 1f));
        drawCentered(titleFont, "JUMPHOOPER", Config.VIEWPORT_HEIGHT * 0.72f);

        // Loading text pulse
        float loadAlpha = 0.5f + 0.5f * MathUtils.sin(elapsed * 3.5f);
        bodyFont.setColor(new Color(0.60f, 0.55f, 0.78f, loadAlpha));
        drawCentered(bodyFont, "loading...", barY - 26f);

        // Percentage
        bodyFont.setColor(new Color(0.45f, 0.88f, 0.86f, 0.7f));
        drawCentered(bodyFont, Math.round(progress * 100f) + "%", barY + barH + 28f);

        batch.end();

        if (loaded && elapsed >= MIN_DURATION) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void ensureTextures() {
        if (dot != null) return;

        Pixmap pix = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fillCircle(2, 2, 2);
        dot = new Texture(pix);
        pix.dispose();

        // Glow texture
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

        // Character (same as before but with more detail)
        int w = 36, h = 44;
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0);
        p.fill();
        int cx = w / 2, cy = h / 2 + 3;

        // Shadow
        p.setColor(new Color(0.05f, 0.30f, 0.32f, 0.4f));
        p.fillCircle(cx + 1, cy + 2, 13);

        // Body
        p.setColor(new Color(0.10f, 0.62f, 0.65f, 1f));
        p.fillCircle(cx, cy, 13);
        p.setColor(new Color(0.15f, 0.78f, 0.78f, 1f));
        p.fillCircle(cx, cy, 12);
        p.setColor(new Color(0.28f, 0.90f, 0.88f, 1f));
        p.fillCircle(cx - 2, cy - 3, 8);
        p.setColor(new Color(0.50f, 0.96f, 0.94f, 0.5f));
        p.fillCircle(cx - 4, cy - 5, 4);

        // Eyes
        p.setColor(Color.WHITE);
        p.fillCircle(cx - 4, cy - 3, 5);
        p.fillCircle(cx + 4, cy - 3, 5);
        p.setColor(new Color(0.05f, 0.05f, 0.10f, 1f));
        p.fillCircle(cx - 4, cy - 4, 2);
        p.fillCircle(cx + 4, cy - 4, 2);
        p.setColor(Color.WHITE);
        p.drawPixel(cx - 3, cy - 5);
        p.drawPixel(cx + 5, cy - 5);

        // Cheeks
        p.setColor(new Color(1f, 0.55f, 0.50f, 0.25f));
        p.fillCircle(cx - 8, cy, 3);
        p.fillCircle(cx + 8, cy, 3);

        // Smile
        p.setColor(new Color(0.06f, 0.30f, 0.33f, 1f));
        p.drawLine(cx - 3, cy + 4, cx, cy + 5);
        p.drawLine(cx, cy + 5, cx + 3, cy + 4);

        // Antennae
        p.setColor(new Color(0.10f, 0.55f, 0.58f, 1f));
        p.drawLine(cx - 3, cy - 12, cx - 6, cy - 17);
        p.drawLine(cx + 3, cy - 12, cx + 6, cy - 17);
        p.setColor(new Color(1f, 0.65f, 0.15f, 1f));
        p.fillCircle(cx - 6, cy - 17, 2);
        p.fillCircle(cx + 6, cy - 17, 2);
        p.setColor(new Color(1f, 0.85f, 0.40f, 0.8f));
        p.fillCircle(cx - 6, cy - 17, 1);
        p.fillCircle(cx + 6, cy - 17, 1);

        // Feet
        p.setColor(new Color(0.10f, 0.58f, 0.60f, 1f));
        p.fillCircle(cx - 5, cy + 13, 3);
        p.fillCircle(cx + 5, cy + 13, 3);

        characterTex = new Texture(p);
        p.dispose();
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
        bodyFont.dispose();
        if (dot != null) dot.dispose();
        if (characterTex != null) characterTex.dispose();
        if (glowTex != null) glowTex.dispose();
    }

    private void drawCentered(BitmapFont font, String text, float y) {
        layout.setText(font, text);
        float x = (Config.VIEWPORT_WIDTH - layout.width) * 0.5f;
        font.draw(batch, layout, x, y);
    }
}
