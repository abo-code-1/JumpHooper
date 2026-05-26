package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.achievements.AchievementManager;
import com.starbots.starjump.achievements.Curiosities;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Button;
import com.starbots.starjump.util.Colors;
import com.starbots.starjump.util.Painter;

/** "Achievements" screen: distance + jumps progress cards, each opening curiosities. */
public final class AchievementsState implements GameState {

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;

    private final float cardX, cardW, cardH;
    private final Button distanceButton;
    private final Button jumpsButton;
    private final Button backButton;

    public AchievementsState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();

        cardW = Config.WORLD_WIDTH * 0.9f;
        cardX = (Config.WORLD_WIDTH - cardW) / 2f;
        cardH = 180f;

        BitmapFont btnFont = a.font(Assets.THALEAH, 20);
        float bw = cardW * 0.8f;
        float bx = cardX + (cardW - bw) / 2f;
        float bh = 40f;
        distanceButton = new Button(bx, 80f + cardH - bh - 16f, bw, bh,
                "Curiosities", btnFont, Colors.OLIVE_TOP, Colors.OLIVE_BOT);
        jumpsButton = new Button(bx, 290f + cardH - bh - 16f, bw, bh,
                "Curiosities", btnFont, Colors.OLIVE_TOP, Colors.OLIVE_BOT);

        float backW = 200f, backH = 48f;
        backButton = new Button((Config.WORLD_WIDTH - backW) / 2f,
                Config.WORLD_HEIGHT - 92f, backW, backH, "Back",
                a.font(Assets.THALEAH, 22), Colors.GRAY_TOP, Colors.GRAY_BOT);
    }

    @Override public void enter() {}

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)
                || backButton.clicked(painter)) {
            game.gsm().set(new MenuState(game));
        } else if (distanceButton.clicked(painter)) {
            game.gsm().set(new CuriositiesState(game, CuriositiesState.Type.DISTANCE));
        } else if (jumpsButton.clicked(painter)) {
            game.gsm().set(new CuriositiesState(game, CuriositiesState.Type.JUMPS));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        int distance = ScoreManager.INSTANCE.getTotalDistance();
        int jumps = ScoreManager.INSTANCE.getTotalJumps();

        // 1) Backdrop.
        painter.begin();
        painter.fullscreen(a.splashBackground);
        painter.end();

        // 2) Card backgrounds + borders + buttons.
        painter.beginShapes();
        painter.rect(cardX, 80f, cardW, cardH, Colors.CARD_BG);
        painter.rect(cardX, 290f, cardW, cardH, Colors.CARD_BG);
        painter.border(cardX, 80f, cardW, cardH, 2f, Colors.WHITE);
        painter.border(cardX, 290f, cardW, cardH, 2f, Colors.WHITE);
        distanceButton.fill(painter);
        jumpsButton.fill(painter);
        backButton.fill(painter);
        painter.endShapes();

        // 3) Text + labels.
        painter.begin();
        painter.textCentered(a.font(Assets.NASALIZATION, 32), "Achievements",
                Config.WORLD_WIDTH / 2f, 28f);
        drawCardText("Distance", distance, 80f);
        drawCardText("Jumps", jumps, 290f);
        distanceButton.drawLabel(painter);
        jumpsButton.drawLabel(painter);
        backButton.drawLabel(painter);
        painter.end();
    }

    private void drawCardText(String label, int total, float cardTop) {
        int progress = AchievementManager.countUnlocked(total);
        int max = Curiosities.MILESTONES.length;
        int next = progress < max ? Curiosities.MILESTONES[progress]
                                  : Curiosities.MILESTONES[max - 1];
        float percent = Math.min(100f, total / (float) next * 100f);
        float cx = Config.WORLD_WIDTH / 2f;

        // Card content is dark (the card itself is a light, translucent panel).
        BitmapFont f20 = a.font(Assets.NASALIZATION, 20);
        BitmapFont f26 = a.font(Assets.NASALIZATION, 26);
        BitmapFont f18 = a.font(Assets.NASALIZATION, 18);
        f20.setColor(Colors.BLACK);
        f26.setColor(Colors.BLACK);
        f18.setColor(Colors.BLACK);

        painter.textCentered(f20, label + " (" + progress + "/" + max + ")", cx, cardTop + 16f);
        painter.textCentered(f26, String.format("%.2f%%", percent), cx, cardTop + 52f);
        painter.textCentered(f18, total + "   →   " + next, cx, cardTop + 92f);

        f20.setColor(Colors.WHITE);
        f26.setColor(Colors.WHITE);
        f18.setColor(Colors.WHITE);
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
