package com.duddlejump;

import com.badlogic.gdx.Game;
import com.duddlejump.screens.MainMenuScreen;

public class DuddleJumpGame extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
