package com.starbots.starjump.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.starbots.starjump.Config;
import com.starbots.starjump.ScoreManager;
import com.starbots.starjump.StarJumpGame;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.model.Player;
import com.starbots.starjump.model.World;
import com.starbots.starjump.model.platform.Platform;
import com.starbots.starjump.patterns.state.GameState;
import com.starbots.starjump.util.Painter;

/**
 * Gameplay. Advances the {@link World} with a fixed 60 Hz timestep so the ported
 * per-frame physics behave exactly like the original, then draws the scene.
 */
public final class PlayState implements GameState {

    private final StarJumpGame game;
    private final Painter painter;
    private final Assets a;
    private final World world;

    private float accumulator;

    public PlayState(StarJumpGame game) {
        this.game = game;
        this.painter = game.painter();
        this.a = game.assets();
        this.world = new World(game.tilt());
    }

    @Override
    public void enter() {
        world.startRun();
        accumulator = 0f;
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            game.gsm().set(new MenuState(game));
            return;
        }

        accumulator += delta;
        while (accumulator >= Config.STEP) {
            world.step();
            accumulator -= Config.STEP;
            if (world.isGameOver()) break;
        }

        if (world.isGameOver()) {
            ScoreManager.INSTANCE.endRun();        // updates record/totals, fires events
            game.gsm().set(new GameOverState(game));
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Player player = world.getPlayer();

        painter.begin();
        painter.fullscreen(a.background);

        for (Platform p : world.getPlatforms()) {
            painter.image(a.platform(p.kind), p.x, p.y, p.width, p.height);
        }

        painter.image(a.astronaut, player.x, player.y, player.width, player.height,
                player.facingLeft);

        painter.textCentered(a.font(Assets.NASALIZATION, 32),
                String.valueOf(ScoreManager.INSTANCE.getScore()),
                Config.WORLD_WIDTH / 2f, 30f);
        painter.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void exit() {}
}
