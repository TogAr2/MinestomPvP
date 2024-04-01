package io.github.togar2.pvp.entity;

import net.minestom.server.coordinate.Vec;

public interface PvpPlayer {
    void jump();
    
    void afterSprintAttack();
    
    void addVelocityNoUpdate(Vec velocity);
    
    void mulVelocityNoUpdate(double factor);
}
