package io.github.bloepiloepi.pvp.entities;

public interface PvpPlayer {
    default double getJumpVelocity() {
        return 0.42;
    }

    double getJumpBoostVelocityModifier();

    void jump();

    void afterSprintAttack();
}
