package com.starbots.starjump.model.enemy;

import com.badlogic.gdx.math.MathUtils;

/**
 * Concrete creator: rolls an enemy whose type/behaviour mix gets nastier as the
 * score climbs (homing "hunters" become more common higher up).
 */
public final class StandardEnemyFactory extends EnemyFactory {

    @Override
    protected Enemy createEnemy(int score) {
        Enemy e = new Enemy();

        // Chance of an aggressive homing hunter grows slowly with score.
        float hunterChance = Math.min(0.35f, 0.05f + score / 60000f);
        boolean hunter = MathUtils.random() < hunterChance;

        if (hunter) {
            e.type = EnemyType.HUNTER;
            e.width = 40f;
            e.height = 34f;
            // A third of hunters dive (home); the rest just float on a sine.
            if (MathUtils.random() < 0.34f) {
                e.setBehavior(DiverBehavior.INSTANCE);
                e.vx = 0f;
            } else {
                e.setBehavior(SineFloaterBehavior.INSTANCE);
                e.vx = MathUtils.random(40f, 65f) * (MathUtils.randomBoolean() ? 1 : -1);
            }
        } else {
            e.type = EnemyType.DRONE;
            e.width = 46f;
            e.height = 34f;
            e.setBehavior(DrifterBehavior.INSTANCE);
            e.vx = MathUtils.random(35f, 60f) * (MathUtils.randomBoolean() ? 1 : -1);
        }
        return e;
    }
}
