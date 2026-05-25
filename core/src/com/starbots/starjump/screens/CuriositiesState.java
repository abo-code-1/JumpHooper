package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.achievements.Curiosities;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Colors;
import com.starbots.starjump.util.Painter;

/**
 * "Curiosidades" screen: a scrollable list of 15 robotics-trivia cards, locked
 * or unlocked depending on the player's lifetime distance / jumps. Drag or use
 * the arrow keys to scroll.
 */
public final class CuriositiesState implements GameState {

    public enum Type { DISTANCE, JUMPS }

    private static final float CONTENT_TOP = 76f;
    private static final float GAP = 14f;
    private static final float PAD = 14f;

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;
    private final Type type;

    private final float cardX, cardW, textW;
    private boolean[] unlocked;
    private String[] body;
    private String[] footer;
    private float[] heights;
    private float totalHeight;

    private float scrollY;
    private boolean dragging;
    private float lastDragY;
    private final Vector2 tmp = new Vector2();

    public CuriositiesState(StarJumpGame game, Type type) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();
        this.type = type;
        this.cardW = Config.WORLD_WIDTH * 0.9f;
        this.cardX = (Config.WORLD_WIDTH - cardW) / 2f;
        this.textW = cardW - 2 * PAD;
    }

    @Override
    public void enter() {
        int[] milestones = Curiosities.MILESTONES;
        String[] prizes = type == Type.DISTANCE
                ? Curiosities.DISTANCE_PRIZES : Curiosities.JUMP_PRIZES;
        int total = type == Type.DISTANCE
                ? ScoreManager.INSTANCE.getTotalDistance()
                : ScoreManager.INSTANCE.getTotalJumps();
        String typeWord = type == Type.DISTANCE ? "distância" : "pulos";

        int n = milestones.length;
        unlocked = new boolean[n];
        body = new String[n];
        footer = new String[n];
        heights = new float[n];

        BitmapFont headerFont = a.font(Assets.NASALIZATION, 20);
        BitmapFont bodyFont = a.font(Assets.MONTSERRAT, 14);
        BitmapFont lockedFont = a.font(Assets.DYSLEXIC, 18);
        BitmapFont footerFont = a.font(Assets.DYSLEXIC, 11);

        totalHeight = CONTENT_TOP;
        for (int i = 0; i < n; i++) {
            unlocked[i] = total >= milestones[i];
            body[i] = (i < prizes.length) ? prizes[i] : "";
            footer[i] = (unlocked[i] ? "Adquirida ao alcançar a pontuação "
                                     : "Adquire-se ao alcançar a pontuação ")
                    + milestones[i] + " em " + typeWord;

            float headerH = headerFont.getCapHeight() + 6;
            float bodyH = unlocked[i]
                    ? painter.wrappedHeight(bodyFont, body[i], textW)
                    : lockedFont.getCapHeight();
            float footerH = painter.wrappedHeight(footerFont, footer[i], textW);
            heights[i] = PAD + headerH + 8 + bodyH + 10 + footerH + PAD;
            totalHeight += heights[i] + GAP;
        }
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)) {
            game.gsm().set(new AchievementsState(game));
            return;
        }

        // Keyboard scroll.
        if (Gdx.input.isKeyPressed(Keys.DOWN)) scrollY += 400 * delta;
        if (Gdx.input.isKeyPressed(Keys.UP))   scrollY -= 400 * delta;

        // Drag scroll (works on touch + mouse).
        if (Gdx.input.isTouched()) {
            painter.pointer(tmp);
            if (dragging) scrollY -= (tmp.y - lastDragY);
            lastDragY = tmp.y;
            dragging = true;
        } else {
            dragging = false;
        }

        float maxScroll = Math.max(0f, totalHeight - Config.WORLD_HEIGHT + 40f);
        scrollY = MathUtils.clamp(scrollY, 0f, maxScroll);
    }

    @Override
    public void render(SpriteBatch batch) {
        // 1) Backdrop.
        painter.begin();
        painter.fullscreen(a.splashBackground);
        painter.end();

        // 2) Card panels.
        painter.beginShapes();
        float cursor = CONTENT_TOP - scrollY;
        for (int i = 0; i < heights.length; i++) {
            if (onScreen(cursor, heights[i])) {
                Color top = unlocked[i] ? Colors.GREEN_TOP : Colors.LOCKED_TOP;
                Color bot = unlocked[i] ? Colors.GREEN_BOT : Colors.LOCKED_BOT;
                painter.gradientRect(cardX, cursor, cardW, heights[i], top, bot);
                painter.border(cardX, cursor, cardW, heights[i], 2f, Colors.WHITE);
            }
            cursor += heights[i] + GAP;
        }
        painter.endShapes();

        // 3) Title + card text.
        painter.begin();
        painter.textCentered(a.font(Assets.NASALIZATION, 32), "Curiosidades",
                Config.WORLD_WIDTH / 2f, 26f);

        BitmapFont headerFont = a.font(Assets.NASALIZATION, 20);
        BitmapFont bodyFont = a.font(Assets.MONTSERRAT, 14);
        BitmapFont lockedFont = a.font(Assets.DYSLEXIC, 18);
        BitmapFont footerFont = a.font(Assets.DYSLEXIC, 11);

        cursor = CONTENT_TOP - scrollY;
        for (int i = 0; i < heights.length; i++) {
            if (onScreen(cursor, heights[i])) {
                float headerH = headerFont.getCapHeight() + 6;
                dark(headerFont, "Curiosidade #" + (i + 1), cardX + PAD, cursor + PAD, false, 0);
                float bodyTop = cursor + PAD + headerH + 8;
                if (unlocked[i]) {
                    dark(bodyFont, body[i], cardX + PAD, bodyTop, true, textW);
                } else {
                    dark(lockedFont, "???", Config.WORLD_WIDTH / 2f, bodyTop, false, -1);
                }
                float footerH = painter.wrappedHeight(footerFont, footer[i], textW);
                dark(footerFont, footer[i], cardX + PAD,
                        cursor + heights[i] - PAD - footerH, true, textW);
            }
            cursor += heights[i] + GAP;
        }

        painter.textCentered(a.font(Assets.DYSLEXIC, 12), "ESC / voltar  •  arraste para rolar",
                Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT - 22f);
        painter.end();
    }

    /** Draw dark text. {@code wrapped} => left-aligned wrap; width<0 => centered. */
    private void dark(BitmapFont font, String s, float x, float top, boolean wrapped, float width) {
        font.setColor(Colors.BLACK);
        if (wrapped) {
            painter.textWrapped(font, s, x, top, width);
        } else if (width < 0) {
            painter.textCentered(font, s, x, top);
        } else {
            painter.text(font, s, x, top);
        }
        font.setColor(Colors.WHITE);
    }

    private boolean onScreen(float top, float h) {
        return top + h >= 0 && top <= Config.WORLD_HEIGHT;
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
