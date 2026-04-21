package com.duddlejump.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.duddlejump.Config;
import com.duddlejump.events.EventBus;
import com.duddlejump.factories.PlatformFactory;
import com.duddlejump.support.HeadlessGdxTestRuntime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DoodleLandingTest {

    @BeforeAll
    static void bootLibGdx() {
        HeadlessGdxTestRuntime.boot();
    }

    @AfterAll
    static void shutdownLibGdx() {
        HeadlessGdxTestRuntime.shutdown();
    }

    @Test
    void landsWhenFallingAcrossPlatformTopInSingleFrame() {
        Texture placeholder = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        Doodle doodle = new Doodle(placeholder, 120f, 150f, true);
        PlatformFactory factory = new PlatformFactory();
        Platform platform = factory.create(PlatformKind.GREEN, 110f, 40f);
        EventBus bus = new EventBus();
        bus.subscribe((d, p) -> p.onContact(d));

        Array<Platform> platforms = new Array<>();
        platforms.add(platform);

        doodle.getVelocity().set(0f, -900f);
        doodle.update(0.12f, () -> 0f);
        doodle.checkContact(platforms, bus);

        float expectedPlatformTop = platform.getBounds().y + platform.getBounds().height;
        assertEquals(expectedPlatformTop, doodle.getPosition().y, 0.001f);
        assertTrue(doodle.getVelocity().y > 0f);
        assertEquals(Config.JUMP_VELOCITY, doodle.getVelocity().y, 0.001f);

        doodle.dispose();
        factory.dispose();
    }
}
