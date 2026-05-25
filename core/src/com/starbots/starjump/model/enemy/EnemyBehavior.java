package com.starbots.starjump.model.enemy;

import com.starbots.starjump.model.Player;

/** Strategy interface for how an enemy moves each frame. */
public interface EnemyBehavior {
    void update(Enemy enemy, Player player, float dt);
}
