package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Button;
import com.starbots.starjump.util.Colors;
import com.starbots.starjump.util.Painter;

/** Game-over screen: StarBots logo, the score, an optional "new record", "Voltar". */
public final class GameOverState implements GameState {

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;
    private final Button backButton;

    public GameOverState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();

        float bw = Config.WORLD_WIDTH * 0.6f;
        float bx = (Config.WORLD_WIDTH - bw) / 2f;
        float bh = 56f;
        BitmapFont btnFont = a.font(Assets.THALEAH, 26);
        backButton = new Button(bx, Config.WORLD_HEIGHT - 130 - bh, bw, bh,
                "Voltar", btnFont, Colors.GRAY_TOP, Colors.GRAY_BOT);
    }

    @Override public void enter() {}

    @Override
    public void update(float delta) {
        if (backButton.clicked(painter)
                || Gdx.input.isKeyJustPressed(Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
                || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            game.gsm().set(new MenuState(game));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        float cx = Config.WORLD_WIDTH / 2f;
        int score = ScoreManager.INSTANCE.getScore();
        boolean newRecord = ScoreManager.INSTANCE.wasNewRecord();

        painter.begin();
        painter.fullscreen(a.splashBackground);

        float logoW = 190f;
        float logoH = logoW * a.starbots.getHeight() / a.starbots.getWidth();
        painter.image(a.starbots, cx - logoW / 2f, 110f, logoW, logoH);

        painter.textCentered(a.font(Assets.DYSLEXIC, 20), "pontuação obtida", cx, 330f);
        painter.textCentered(a.font(Assets.NASALIZATION, 46), String.valueOf(score), cx, 356f);

        if (newRecord) {
            BitmapFont rec = a.font(Assets.THALEAH, 32);
            rec.setColor(Colors.NEW_RECORD);
            painter.textCentered(rec, "Novo recorde!", cx, 430f);
            rec.setColor(Colors.WHITE);
        }
        painter.end();

        painter.beginShapes();
        backButton.fill(painter);
        painter.endShapes();

        painter.begin();
        backButton.drawLabel(painter);
        painter.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
