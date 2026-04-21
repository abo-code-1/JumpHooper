package com.duddlejump.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.duddlejump.Config;
import com.duddlejump.DuddleJumpGame;
import com.duddlejump.entities.Doodle;
import com.duddlejump.entities.Platform;
import com.duddlejump.entities.PlatformSpawner;
import com.duddlejump.events.EventBus;
import com.duddlejump.factories.PlatformFactory;
import com.duddlejump.input.InputController;
import com.duddlejump.input.KeyboardInputController;

public class GameScreen extends ScreenAdapter {

    private final DuddleJumpGame game;
    private final FitViewport viewport;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;
    private final InputController input;
    private final EventBus bus;
    private final PlatformFactory platformFactory;
    private final PlatformSpawner spawner;
    private final Doodle doodle;

    private float highestY;

    public GameScreen(DuddleJumpGame game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Config.VIEWPORT_WIDTH, Config.VIEWPORT_HEIGHT, camera);
        this.batch = new SpriteBatch();
        this.input = new KeyboardInputController();
        this.bus = new EventBus();
        this.platformFactory = new PlatformFactory();
        this.spawner = new PlatformSpawner(platformFactory);

        Texture sprite = createPlaceholderSprite();
        float startX = (Config.VIEWPORT_WIDTH - Doodle.WIDTH) * 0.5f;
        float startY = Config.VIEWPORT_HEIGHT * 0.25f;
        this.doodle = new Doodle(sprite, startX, startY, true);
        this.doodle.bounce();
        this.highestY = startY;

        camera.position.set(Config.VIEWPORT_WIDTH * 0.5f, Config.VIEWPORT_HEIGHT * 0.5f, 0f);
        camera.update();

        spawner.seed(startY - 40f, camera.position.y + Config.VIEWPORT_HEIGHT);

        bus.subscribe((d, p) -> p.onContact(d));
    }

    @Override
    public void show() {
        viewport.apply(true);
    }

    @Override
    public void render(float delta) {
        doodle.update(delta, input);

        float cameraTop = camera.position.y + Config.VIEWPORT_HEIGHT * 0.5f;
        float cameraBottom = camera.position.y - Config.VIEWPORT_HEIGHT * 0.5f;

        spawner.update(delta, cameraBottom, cameraTop);
        doodle.checkContact(spawner.getPlatforms(), bus);

        followCamera();

        if (doodle.getPosition().y > highestY) {
            highestY = doodle.getPosition().y;
        }

        ScreenUtils.clear(0.97f, 0.95f, 0.88f, 1.0f);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        Array<Platform> platforms = spawner.getPlatforms();
        for (int i = 0; i < platforms.size; i++) {
            platforms.get(i).render(batch);
        }
        doodle.render(batch);
        batch.end();
    }

    public float getHighestY() {
        return highestY;
    }

    public float getCameraBottom() {
        return camera.position.y - Config.VIEWPORT_HEIGHT * 0.5f;
    }

    private void followCamera() {
        float threshold = camera.position.y;
        if (doodle.getPosition().y > threshold) {
            camera.position.y = doodle.getPosition().y;
            camera.update();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void dispose() {
        doodle.dispose();
        platformFactory.dispose();
        batch.dispose();
    }

    private static Texture createPlaceholderSprite() {
        int w = 56;
        int h = 64;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();
        pixmap.setColor(new Color(0.36f, 0.72f, 0.36f, 1f));
        pixmap.fillCircle(w / 2, h / 2, Math.min(w, h) / 2 - 2);
        pixmap.setColor(Color.BLACK);
        pixmap.fillCircle((int) (w * 0.35f), (int) (h * 0.4f), 4);
        pixmap.fillCircle((int) (w * 0.65f), (int) (h * 0.4f), 4);
        pixmap.setColor(new Color(0.2f, 0.5f, 0.2f, 1f));
        pixmap.fillRectangle(w / 2 - 2, (int) (h * 0.75f), 4, 6);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
