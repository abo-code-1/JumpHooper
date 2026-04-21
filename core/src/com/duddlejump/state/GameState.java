package com.duddlejump.state;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface GameState {

    void enter();

    void update(float dt);

    void render(SpriteBatch batch);

    void exit();
}
