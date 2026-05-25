package com.starbots.starjump;

import com.starbots.starjump.input.TiltControl;
import com.starbots.starjump.model.Player;
import com.starbots.starjump.model.World;
import com.starbots.starjump.model.platform.MovingBehavior;
import com.starbots.starjump.model.platform.Platform;
import com.starbots.starjump.model.platform.PlatformKind;
import com.starbots.starjump.patterns.observer.EventBus;
import com.starbots.starjump.patterns.observer.GameEvent;
import com.starbots.starjump.patterns.observer.GameEventListener;
import com.starbots.starjump.patterns.observer.GameEventType;

import java.util.EnumMap;

/**
 * Headless, GL-free smoke test of the simulation. Runs the {@link World} for
 * many games with an autopilot {@link TiltControl}, then asserts that the
 * ported mechanics behave: the player jumps, scores climb past the lava unlock,
 * every platform effect appears, deaths happen, and the Observer bus fires.
 *
 * <p>Run with: {@code ./gradlew smokeTest}</p>
 */
public final class SmokeTest {

    /** Autopilot: steer horizontally toward the vertically-nearest platform. */
    private static final class Autopilot implements TiltControl {
        World world;
        @Override public float getTilt() {
            if (world == null) return 0f;
            Player p = world.getPlayer();
            float playerCenter = p.x + p.width / 2f;
            Platform best = null;
            float bestDy = Float.MAX_VALUE;
            for (Platform pl : world.getPlatforms()) {
                if (pl.kind == PlatformKind.LAVA) continue; // never aim at lava
                float dy = Math.abs(pl.y - p.bottom());
                if (dy < bestDy) { bestDy = dy; best = pl; }
            }
            if (best == null) return 0f;
            float targetCenter = best.x + best.width / 2f;
            float dx = targetCenter - playerCenter;
            if (Math.abs(dx) < 4f) return 0f;
            // world does x += tilt * MOVE_FACTOR (MOVE_FACTOR < 0): go right => tilt < 0.
            return dx > 0 ? -0.5f : 0.5f;
        }
    }

    public static void main(String[] args) {
        final int games = 40;
        final int stepCap = 12000;

        // Wire the Observer bus into the ScoreManager (headless: persistence skipped).
        EventBus bus = new EventBus();
        final int[] jumpEvents = {0};
        final int[] deathEvents = {0};
        bus.subscribe(new GameEventListener() {
            @Override public void onGameEvent(GameEvent e) {
                if (e.type == GameEventType.PLAYER_JUMPED) jumpEvents[0]++;
                if (e.type == GameEventType.PLAYER_DIED) deathEvents[0]++;
            }
        });
        ScoreManager.INSTANCE.init(bus);

        Autopilot autopilot = new Autopilot();
        World world = new World(autopilot);
        autopilot.world = world;

        int maxScore = 0;
        long totalSteps = 0;
        int gamesOver = 0;
        int gamesPast600 = 0;
        boolean[] movingSeen = {false};
        EnumMap<PlatformKind, Integer> kindSeen = new EnumMap<>(PlatformKind.class);

        for (int g = 0; g < games; g++) {
            world.startRun();
            int steps = 0;
            while (!world.isGameOver() && steps < stepCap) {
                world.step();
                steps++;
                for (Platform pl : world.getPlatforms()) {
                    kindSeen.merge(pl.kind, 1, Integer::sum);
                    if (pl.getBehavior() == MovingBehavior.INSTANCE) movingSeen[0] = true;
                }
            }
            totalSteps += steps;
            int score = ScoreManager.INSTANCE.getScore();
            maxScore = Math.max(maxScore, score);
            if (world.isGameOver()) gamesOver++;
            if (score > Config.LAVA_UNLOCK_SCORE) gamesPast600++;
            // Fold into lifetime totals (exercises the Singleton path, bus is null-safe).
            ScoreManager.INSTANCE.endRun();
        }

        System.out.println("=== StarJump simulation smoke test ===");
        System.out.println("games:            " + games);
        System.out.println("total steps:      " + totalSteps);
        System.out.println("games ended:      " + gamesOver + "/" + games);
        System.out.println("games past 600:   " + gamesPast600);
        System.out.println("max score:        " + maxScore);
        System.out.println("jump events:      " + jumpEvents[0]);
        System.out.println("death events:     " + deathEvents[0]);
        System.out.println("lifetime jumps:   " + ScoreManager.INSTANCE.getTotalJumps());
        System.out.println("platform kinds:   " + kindSeen);

        boolean ok = true;
        ok &= check("player jumped", jumpEvents[0] > 0);
        ok &= check("deaths fired on bus", deathEvents[0] > 0);
        ok &= check("score climbs past lava unlock (600)", maxScore > Config.LAVA_UNLOCK_SCORE);
        ok &= check("ground platforms appear",
                kindSeen.getOrDefault(PlatformKind.GRASS, 0)
                        + kindSeen.getOrDefault(PlatformKind.GRASS_BROKEN, 0)
                        + kindSeen.getOrDefault(PlatformKind.STONE, 0)
                        + kindSeen.getOrDefault(PlatformKind.STONE_BROKEN, 0) > 0);
        ok &= check("moving platforms appear (Strategy effect roll)", movingSeen[0]);
        ok &= check("lava platforms appear after 600", kindSeen.getOrDefault(PlatformKind.LAVA, 0) > 0);
        ok &= check("games actually end", gamesOver > 0);

        System.out.println(ok ? "\nSMOKE TEST PASSED" : "\nSMOKE TEST FAILED");
        if (!ok) System.exit(1);
    }

    private static boolean check(String label, boolean cond) {
        System.out.println((cond ? "  [PASS] " : "  [FAIL] ") + label);
        return cond;
    }
}
