package com.duddlejump;

import com.badlogic.gdx.Game;
import com.duddlejump.screens.GameScreen;

public class DuddleJumpGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
