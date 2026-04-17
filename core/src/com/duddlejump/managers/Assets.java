package com.duddlejump.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public final class Assets implements Disposable {

    private final AssetManager manager = new AssetManager();

    public <T> void queue(String path, Class<T> type) {
        manager.load(path, type);
    }

    public boolean update() {
        return manager.update();
    }

    public float progress() {
        return manager.getProgress();
    }

    public void finishLoading() {
        manager.finishLoading();
    }

    public Texture texture(String path) {
        return manager.get(path, Texture.class);
    }

    public Sound sound(String path) {
        return manager.get(path, Sound.class);
    }

    public <T> T get(String path, Class<T> type) {
        return manager.get(path, type);
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}
