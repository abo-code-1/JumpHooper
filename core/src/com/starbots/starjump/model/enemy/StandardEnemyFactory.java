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

        // Chance of an aggressive homing hunter grows with score.
        float hunterChance = Math.min(0.5f, 0.12f + score / 30000f);
        boolean hunter = MathUtils.random() < hunterChance;

        if (hunter) {
            e.type = EnemyType.HUNTER;
            e.width = 40f;
            e.height = 34f;
            // Half hunters dive (home), half float on a sine.
            if (MathUtils.randomBoolean()) {
                e.setBehavior(DiverBehavior.INSTANCE);
                e.vx = 0f;
            } else {
                e.setBehavior(SineFloaterBehavior.INSTANCE);
                e.vx = MathUtils.random(60f, 95f) * (MathUtils.randomBoolean() ? 1 : -1);
            }
        } else {
            e.type = EnemyType.DRONE;
            e.width = 46f;
            e.height = 34f;
            e.setBehavior(DrifterBehavior.INSTANCE);
            e.vx = MathUtils.random(55f, 90f) * (MathUtils.randomBoolean() ? 1 : -1);
        }
        return e;
    }
}
