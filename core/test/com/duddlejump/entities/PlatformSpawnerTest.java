package com.duddlejump.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.Config;
import com.duddlejump.factories.PlatformFactory;
import com.duddlejump.support.HeadlessGdxTestRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PlatformSpawnerTest {

    @BeforeAll
    static void bootLibGdx() {
        HeadlessGdxTestRuntime.boot();
    }

    @AfterAll
    static void shutdownLibGdx() {
        HeadlessGdxTestRuntime.shutdown();
    }

    @Test
    void generatesReachablePlatformChains() {
        MathUtils.random.setSeed(12345L);

        PlatformFactory factory = new PlatformFactory();
        PlatformSpawner spawner = new PlatformSpawner(factory);
        spawner.seed(160f, 1200f);

        Array<Platform> platforms = spawner.getPlatforms();
        assertTrue(platforms.size > 3);

        Platform first = platforms.first();
        assertEquals(PlatformKind.GREEN, first.getKind());

        float previousY = first.getBounds().y;
        float previousCenterX = first.getBounds().x + first.getBounds().width * 0.5f;

        for (int i = 1; i < platforms.size; i++) {
            Platform current = platforms.get(i);
            float currentY = current.getBounds().y;
            float currentCenterX = current.getBounds().x + current.getBounds().width * 0.5f;

            float verticalGap = currentY - previousY;
            float horizontalShift = Math.abs(currentCenterX - previousCenterX);

            assertTrue(verticalGap >= 90f && verticalGap <= 150f,
                "Vertical gap out of range at index " + i + ": " + verticalGap);
            assertTrue(horizontalShift <= 150f,
                "Horizontal shift too large at index " + i + ": " + horizontalShift);
            assertTrue(current.getBounds().x >= 0f);
            assertTrue(current.getBounds().x <= Config.WORLD_WIDTH - Platform.WIDTH);

            previousY = currentY;
            previousCenterX = currentCenterX;
        }

        factory.dispose();
    }
}
