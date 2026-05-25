package com.starbots.starjump;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.starbots.starjump.achievements.AchievementManager;
import com.starbots.starjump.assets.Assets;
import com.starbots.starjump.audio.SoundController;
import com.starbots.starjump.input.TiltControl;
import com.starbots.starjump.patterns.observer.EventBus;
import com.starbots.starjump.patterns.state.GameStateManager;
import com.starbots.starjump.screens.LoadingState;
import com.starbots.starjump.util.Painter;

/**
 * Application entry point. Owns the long-lived, shared resources and wires up
 * the patterns:
 * <ul>
 *   <li><b>Adapter</b> — the platform-specific {@link TiltControl} is injected
 *       by each launcher (keyboard on desktop, accelerometer on android).</li>
 *   <li><b>Observer</b> — the {@link EventBus} plus the {@link SoundController}
 *       and {@link AchievementManager} listeners.</li>
 *   <li><b>Singleton</b> — {@link ScoreManager#INSTANCE} is initialised here.</li>
 *   <li><b>State</b> — the {@link GameStateManager} drives the active screen.</li>
 * </ul>
 *
 * <p>Everything except {@code ScoreManager.INSTANCE} is an injected instance,
 * not global state.</p>
 */
public final class StarJumpGame extends ApplicationAdapter {

    private final TiltControl tilt;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Assets assets;
    private Painter painter;
    private EventBus bus;
    private GameStateManager gsm;

    // Observers kept referenced so they aren't collected.
    private SoundController soundController;
    private AchievementManager achievementManager;

    /** @param tilt platform-specific input adapter (Adapter pattern). */
    public StarJumpGame(TiltControl tilt) {
        this.tilt = tilt;
    }

    @Override
    public void create() {
        // Stretch the virtual world height to the real screen aspect first, so
        // the FitViewport fills the device top-to-bottom with no letterbox bars.
        Config.adaptToScreen(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera = new OrthographicCamera();
        viewport = new FitViewport(Config.WORLD_WIDTH, Config.WORLD_HEIGHT, camera);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        assets = new Assets();
        assets.load();

        painter = new Painter(batch, shapes, viewport);

        bus = new EventBus();
        ScoreManager.INSTANCE.init(bus);

        soundController = new SoundController(assets);
        achievementManager = new AchievementManager(bus);
        bus.subscribe(soundController);
        bus.subscribe(achievementManager);

        gsm = new GameStateManager();
        gsm.set(new LoadingState(this));
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.25f);
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        gsm.update(delta);
        gsm.render(batch);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (gsm != null) gsm.resize(width, height);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (assets != null) assets.dispose();
    }

    // --- accessors used by states ---------------------------------------------

    public TiltControl tilt()         { return tilt; }
    public Assets assets()            { return assets; }
    public Painter painter()          { return painter; }
    public EventBus bus()             { return bus; }
    public GameStateManager gsm()     { return gsm; }
    public Viewport viewport()        { return viewport; }
}
