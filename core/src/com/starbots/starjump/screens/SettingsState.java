package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.starbots.starjump.Config;
import com.starbots.starjump.GameSettings;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.input.AccelerometerTiltAdapter;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Button;
import com.starbots.starjump.util.Colors;
import com.starbots.starjump.util.Painter;

/** Player-facing settings for sound, music and mobile tilt controls. */
public final class SettingsState implements GameState {

    private static final float STEP_VOLUME = 0.1f;
    private static final float STEP_TILT = 0.1f;

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;
    private final GameSettings settings = GameSettings.INSTANCE;
    private final GameState returnState; // where "back" goes; null -> main menu

    private final Button sfxMinus, sfxPlus;
    private final Button musicMinus, musicPlus;
    private final Button tiltMinus, tiltPlus;
    private final Button controlModeButton;
    private final Button invertButton;
    private final Button axisButton;
    private final Button centerButton;
    private final Button resetCenterButton;
    private final Button backButton;

    public SettingsState(StarJumpGame game) {
        this(game, null);
    }

    /** @param returnState screen to go back to; {@code null} returns to the menu. */
    public SettingsState(StarJumpGame game, GameState returnState) {
        this.game = game;
        this.returnState = returnState;
        this.painter = game.painter();
        this.a = game.assets();

        BitmapFont small = a.font(Assets.THALEAH, 22);
        BitmapFont wide = a.font(Assets.THALEAH, 20);

        float rowX = 54f;
        float plusX = Config.WORLD_WIDTH - 96f;
        float minusX = plusX - 58f;
        float btn = 44f;
        sfxMinus = stepButton(minusX, 164f, btn, "-", small);
        sfxPlus = stepButton(plusX, 164f, btn, "+", small);
        musicMinus = stepButton(minusX, 244f, btn, "-", small);
        musicPlus = stepButton(plusX, 244f, btn, "+", small);
        tiltMinus = stepButton(minusX, 324f, btn, "-", small);
        tiltPlus = stepButton(plusX, 324f, btn, "+", small);
        controlModeButton = new Button(rowX, 388f, Config.WORLD_WIDTH - rowX * 2f, 44f,
                "", wide, Colors.YELLOW_TOP, Colors.YELLOW_BOT);
        invertButton = new Button(rowX, 442f, Config.WORLD_WIDTH - rowX * 2f, 44f,
                "", wide, Colors.GRAY_TOP, Colors.GRAY_BOT);
        axisButton = new Button(rowX, 496f, Config.WORLD_WIDTH - rowX * 2f, 44f,
                "", wide, Colors.GRAY_TOP, Colors.GRAY_BOT);
        centerButton = new Button(rowX, 550f, (Config.WORLD_WIDTH - rowX * 2f - 12f) * 0.56f, 44f,
                "Centralizar", wide, Colors.YELLOW_TOP, Colors.YELLOW_BOT);
        resetCenterButton = new Button(centerButton.x + centerButton.w + 12f, 550f,
                (Config.WORLD_WIDTH - rowX * 2f - 12f) * 0.44f, 44f,
                "Reset", wide, Colors.GRAY_TOP, Colors.GRAY_BOT);
        backButton = new Button(rowX, Config.WORLD_HEIGHT - 96f,
                Config.WORLD_WIDTH - rowX * 2f, 52f, "Voltar", wide,
                Colors.GRAY_TOP, Colors.GRAY_BOT);
    }

    private Button stepButton(float x, float top, float size, String label, BitmapFont font) {
        return new Button(x, top, size, size, label, font, Colors.GRAY_TOP, Colors.GRAY_BOT);
    }

    @Override public void enter() {}

    @Override
    public void update(float delta) {
        layoutButtons(painter.safeTopInset());
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACK)) {
            game.gsm().set(backTarget());
            return;
        }

        if (sfxMinus.clicked(painter)) settings.setSfxVolume(settings.getSfxVolume() - STEP_VOLUME);
        if (sfxPlus.clicked(painter)) settings.setSfxVolume(settings.getSfxVolume() + STEP_VOLUME);
        if (musicMinus.clicked(painter)) settings.setMusicVolume(settings.getMusicVolume() - STEP_VOLUME);
        if (musicPlus.clicked(painter)) settings.setMusicVolume(settings.getMusicVolume() + STEP_VOLUME);
        if (tiltMinus.clicked(painter)) settings.setTiltSensitivity(settings.getTiltSensitivity() - STEP_TILT);
        if (tiltPlus.clicked(painter)) settings.setTiltSensitivity(settings.getTiltSensitivity() + STEP_TILT);
        if (controlModeButton.clicked(painter)) settings.nextControlMode();
        if (invertButton.clicked(painter)) settings.toggleTiltInverted();
        if (axisButton.clicked(painter)) settings.nextTiltAxis();
        if (centerButton.clicked(painter)) {
            settings.setTiltCenter(AccelerometerTiltAdapter.rawAxis(settings.getTiltAxis()));
        }
        if (resetCenterButton.clicked(painter)) settings.resetTiltCenter();
        if (backButton.clicked(painter)) game.gsm().set(backTarget());
    }

    private GameState backTarget() {
        return returnState != null ? returnState : new MenuState(game);
    }

    @Override
    public void render(SpriteBatch batch) {
        float safeTop = painter.safeTopInset();
        layoutButtons(safeTop);
        controlModeButton.label = "Controle: " + settings.getControlModeLabel();
        invertButton.label = settings.isTiltInverted() ? "Direcao: invertida" : "Direcao: normal";
        axisButton.label = "Eixo do tilt: " + settings.getTiltAxisLabel();

        painter.begin();
        painter.fullscreen(a.splashBackground);
        painter.end();

        painter.beginShapes();
        painter.rect(28f, 86f + safeTop, Config.WORLD_WIDTH - 56f, 560f,
                new Color(0f, 0f, 0f, 0.42f));
        sfxMinus.fill(painter); sfxPlus.fill(painter);
        musicMinus.fill(painter); musicPlus.fill(painter);
        tiltMinus.fill(painter); tiltPlus.fill(painter);
        controlModeButton.fill(painter);
        invertButton.fill(painter);
        axisButton.fill(painter);
        centerButton.fill(painter);
        resetCenterButton.fill(painter);
        backButton.fill(painter);
        painter.endShapes();

        painter.begin();
        painter.textCentered(a.font(Assets.NASALIZATION, 32), "Ajustes",
                Config.WORLD_WIDTH / 2f, 34f + safeTop);
        drawRow("Som do jogo", percent(settings.getSfxVolume()), 144f, safeTop);
        drawRow("Musica", percent(settings.getMusicVolume()), 224f, safeTop);
        drawRow("Tilt", String.format("%.1fx", settings.getTiltSensitivity()), 304f, safeTop);
        sfxMinus.drawLabel(painter); sfxPlus.drawLabel(painter);
        musicMinus.drawLabel(painter); musicPlus.drawLabel(painter);
        tiltMinus.drawLabel(painter); tiltPlus.drawLabel(painter);
        controlModeButton.drawLabel(painter);
        invertButton.drawLabel(painter);
        axisButton.drawLabel(painter);
        centerButton.drawLabel(painter);
        resetCenterButton.drawLabel(painter);
        drawSensorReadout(612f, safeTop);
        backButton.drawLabel(painter);
        painter.textCentered(a.font(Assets.DYSLEXIC, 12), "ESC / voltar",
                Config.WORLD_WIDTH / 2f, Config.WORLD_HEIGHT - 24f);
        painter.end();
    }

    private void layoutButtons(float safeTop) {
        sfxMinus.top = 164f + safeTop; sfxPlus.top = 164f + safeTop;
        musicMinus.top = 244f + safeTop; musicPlus.top = 244f + safeTop;
        tiltMinus.top = 324f + safeTop; tiltPlus.top = 324f + safeTop;
        controlModeButton.top = 388f + safeTop;
        invertButton.top = 442f + safeTop;
        axisButton.top = 496f + safeTop;
        centerButton.top = 550f + safeTop;
        resetCenterButton.top = 550f + safeTop;
    }

    private void drawRow(String label, String value, float top, float safeTop) {
        BitmapFont labelFont = a.font(Assets.NASALIZATION, 19);
        BitmapFont valueFont = a.font(Assets.NASALIZATION, 22);
        labelFont.setColor(Colors.WHITE);
        valueFont.setColor(Colors.WHITE);
        painter.text(labelFont, label, 54f, top + safeTop);
        painter.textCentered(valueFont, value, Config.WORLD_WIDTH / 2f + 34f, top + 2f + safeTop);
    }

    private String percent(float value) {
        return Math.round(value * 100f) + "%";
    }

    private void drawSensorReadout(float top, float safeTop) {
        BitmapFont font = a.font(Assets.DYSLEXIC, 11);
        font.setColor(Colors.WHITE);
        float selected = AccelerometerTiltAdapter.rawAxis(settings.getTiltAxis());
        String text = String.format("Sensor %s: %.2f   Centro: %.2f",
                settings.getTiltAxisLabel(), selected, settings.getTiltCenter());
        painter.textCentered(font, text, Config.WORLD_WIDTH / 2f, top + safeTop);
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
