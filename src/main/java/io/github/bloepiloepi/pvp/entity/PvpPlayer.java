package io.github.bloepiloepi.pvp.entity;

import net.minestom.server.coordinate.Vec;

public interface PvpPlayer {
    void jump();
    
    void afterSprintAttack();
    
    void addVelocity(Vec velocity);
    
    void mulVelocity(double factor);
}
