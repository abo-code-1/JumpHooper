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

/** Start screen: logo, record, decorative astronaut, and the two buttons. */
public final class MenuState implements GameState {

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;

    private final Button startButton;
    private final Button achievementsButton;
    private final Button settingsButton;

    public MenuState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();

        float bw = Config.WORLD_WIDTH * 0.6f;
        float bx = (Config.WORLD_WIDTH - bw) / 2f;
        float bh = 48f;
        BitmapFont btnFont = a.font(Assets.THALEAH, 26);

        startButton = new Button(bx, Config.WORLD_HEIGHT - 196f, bw, bh,
                "Iniciar", btnFont, Colors.YELLOW_TOP, Colors.YELLOW_BOT);
        achievementsButton = new Button(bx, Config.WORLD_HEIGHT - 138f, bw, bh,
                "Conquistas", btnFont, Colors.GRAY_TOP, Colors.GRAY_BOT);
        settingsButton = new Button(bx, Config.WORLD_HEIGHT - 80f, bw, bh,
                "Ajustes", btnFont, Colors.GRAY_TOP, Colors.GRAY_BOT);
    }

    @Override public void enter() {}

    @Override
    public void update(float delta) {
        if (startButton.clicked(painter)
                || Gdx.input.isKeyJustPressed(Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            game.gsm().set(new PlayState(game));
        } else if (achievementsButton.clicked(painter)
                || Gdx.input.isKeyJustPressed(Keys.C)) {
            game.gsm().set(new AchievementsState(game));
        } else if (settingsButton.clicked(painter)
                || Gdx.input.isKeyJustPressed(Keys.S)) {
            game.gsm().set(new SettingsState(game));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        float cx = Config.WORLD_WIDTH / 2f;
        int record = ScoreManager.INSTANCE.getRecord();

        painter.begin();
        painter.fullscreen(a.startBackground);

        // Logo wordmark near the top, keeping its aspect ratio.
        float logoW = 240f;
        float logoH = logoW * a.logo.getHeight() / a.logo.getWidth();
        painter.image(a.logo, cx - logoW / 2f, 36f, logoW, logoH);

        painter.textCentered(a.font(Assets.NASALIZATION, 20), "recorde", cx, 226f);
        painter.textCentered(a.font(Assets.NASALIZATION, 48), String.valueOf(record), cx, 252f);

        // Decorative astronaut (original: [width/2 - 22, height/2 + 70]).
        painter.image(a.astronaut, cx - 22f, Config.WORLD_HEIGHT / 2f + 70f,
                Config.PLAYER_W, Config.PLAYER_H);
        painter.end();

        painter.beginShapes();
        startButton.fill(painter);
        achievementsButton.fill(painter);
        settingsButton.fill(painter);
        painter.endShapes();

        painter.begin();
        startButton.drawLabel(painter);
        achievementsButton.drawLabel(painter);
        settingsButton.drawLabel(painter);
        painter.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
