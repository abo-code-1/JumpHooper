package com.duddlejump.e2e;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input.Keys;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.managers.ScoreManager;
import com.duddlejump.screens.GameScreen;
import com.duddlejump.screens.MainMenuScreen;
import com.duddlejump.state.GameOverState;
import com.duddlejump.state.PausedState;
import com.duddlejump.state.PlayingState;
import com.duddlejump.support.HeadlessGdxTestRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameFlowE2ETest {

    private DuddleJumpGame game;

    @BeforeAll
    static void bootLibGdx() {
        HeadlessGdxTestRuntime.boot();
    }

    @AfterAll
    static void shutdownLibGdx() {
        HeadlessGdxTestRuntime.shutdown();
    }

    @BeforeEach
    void setUp() {
        HeadlessGdxTestRuntime.input().reset();
        ScoreManager.INSTANCE.resetForTests();
        game = new DuddleJumpGame();
        game.create();
    }

    @AfterEach
    void tearDown() {
        if (game != null) {
            game.dispose();
        }
        HeadlessGdxTestRuntime.input().reset();
        ScoreManager.INSTANCE.resetForTests();
    }

    @Test
    void launchesIntoMenuAndStartsGameplay() {
        HeadlessGdxTestRuntime.renderFrames(game, 10, 0.1f);
        assertInstanceOf(MainMenuScreen.class, game.getScreen());

        HeadlessGdxTestRuntime.input().pressKey(Keys.SPACE);
        HeadlessGdxTestRuntime.renderFrames(game, 1, 1f / 60f);

        assertInstanceOf(GameScreen.class, game.getScreen());
        assertInstanceOf(PlayingState.class, ((GameScreen) game.getScreen()).getCurrentState());
    }

    @Test
    void pausesAndResumesDuringGameplay() {
        GameScreen screen = startGameplay();

        HeadlessGdxTestRuntime.input().pressKey(Keys.P);
        HeadlessGdxTestRuntime.renderFrames(game, 1, 1f / 60f);
        HeadlessGdxTestRuntime.input().releaseKey(Keys.P);

        assertInstanceOf(PausedState.class, screen.getCurrentState());

        HeadlessGdxTestRuntime.input().pressKey(Keys.P);
        HeadlessGdxTestRuntime.renderFrames(game, 1, 1f / 60f);
        HeadlessGdxTestRuntime.input().releaseKey(Keys.P);

        assertInstanceOf(PlayingState.class, screen.getCurrentState());
    }

    @Test
    void reachesGameOverAndRestarts() {
        GameScreen screen = startGameplay();

        screen.getDoodle().setPosition(
            screen.getDoodle().getPosition().x,
            screen.getCamera().position.y - Config.VIEWPORT_HEIGHT
        );
        HeadlessGdxTestRuntime.renderFrames(game, 1, 1f / 60f);

        assertTrue(screen.isGameOverQueued());
        assertInstanceOf(GameOverState.class, screen.getCurrentState());

        HeadlessGdxTestRuntime.renderFrames(game, 40, 1f / 60f);
        HeadlessGdxTestRuntime.input().pressKey(Keys.SPACE);
        HeadlessGdxTestRuntime.renderFrames(game, 1, 1f / 60f);

        assertInstanceOf(GameScreen.class, game.getScreen());
        assertTrue(game.getScreen() != screen);
        assertInstanceOf(PlayingState.class, ((GameScreen) game.getScreen()).getCurrentState());
    }

    private GameScreen startGameplay() {
        HeadlessGdxTestRuntime.renderFrames(game, 10, 0.1f);
        HeadlessGdxTestRuntime.input().pressKey(Keys.SPACE);
        HeadlessGdxTestRuntime.renderFrames(game, 1, 1f / 60f);
        HeadlessGdxTestRuntime.input().releaseKey(Keys.SPACE);
        return (GameScreen) game.getScreen();
    }
}
