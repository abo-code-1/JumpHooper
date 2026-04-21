package com.duddlejump.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.Config;
import com.duddlejump.factories.PlatformFactory;

public final class PlatformSpawner {

    private static final float MIN_VERTICAL_GAP = 90f;
    private static final float MAX_VERTICAL_GAP = 150f;
    private static final float DESPAWN_MARGIN = 120f;

    private final PlatformFactory factory;
    private final Array<Platform> platforms = new Array<>();
    private float nextSpawnY;

    public PlatformSpawner(PlatformFactory factory) {
        this.factory = factory;
    }

    public void seed(float startY, float topY) {
        platforms.clear();
        float anchorX = (Config.WORLD_WIDTH - Platform.WIDTH) * 0.5f;
        platforms.add(factory.create(PlatformKind.GREEN, anchorX, startY));
        nextSpawnY = startY + MathUtils.random(MIN_VERTICAL_GAP, MAX_VERTICAL_GAP);
        fillUpTo(topY);
    }

    public void update(float dt, float cameraBottom, float cameraTop) {
        fillUpTo(cameraTop + DESPAWN_MARGIN);
        for (int i = 0; i < platforms.size; i++) {
            platforms.get(i).update(dt);
        }
        for (int i = platforms.size - 1; i >= 0; i--) {
            Platform p = platforms.get(i);
            if (p.isDestroyed() || p.getBounds().y + p.getBounds().height < cameraBottom - DESPAWN_MARGIN) {
                platforms.removeIndex(i);
            }
        }
    }

    public Array<Platform> getPlatforms() {
        return platforms;
    }

    private void fillUpTo(float topY) {
        while (nextSpawnY < topY) {
            float x = MathUtils.random(0f, Config.WORLD_WIDTH - Platform.WIDTH);
            float altitudeFactor = MathUtils.clamp(nextSpawnY / 6000f, 0f, 1f);
            float whiteProb = 0.08f + 0.12f * altitudeFactor;
            float redProb = 0.05f + 0.20f * altitudeFactor;
            float blueProb = 0.10f + 0.20f * altitudeFactor;
            platforms.add(factory.random(x, nextSpawnY, redProb, blueProb, whiteProb));
            nextSpawnY += MathUtils.random(MIN_VERTICAL_GAP, MAX_VERTICAL_GAP);
        }
    }
}
