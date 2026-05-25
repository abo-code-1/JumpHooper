package com.starbots.starjump.model.enemy;

/**
 * Creator of the Factory Method pattern for enemies. {@link #spawn} positions a
 * product built by the abstract {@link #createEnemy} factory method.
 */
public abstract class EnemyFactory {

    public final Enemy spawn(float x, float y, int score) {
        Enemy e = createEnemy(score);
        e.x = x;
        e.y = y;
        e.phase = 0f;
        return e;
    }

    /** Factory method: subclasses decide the enemy type, size and behaviour. */
    protected abstract Enemy createEnemy(int score);
}
