package com.duddlejump;

import com.badlogic.gdx.Game;
import com.duddlejump.managers.Assets;
import com.duddlejump.screens.LoadingScreen;

public class DuddleJumpGame extends Game {

    private Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        setScreen(new LoadingScreen(this));
    }

    public Assets getAssets() {
        return assets;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assets != null) {
            assets.dispose();
        }
    }
}
