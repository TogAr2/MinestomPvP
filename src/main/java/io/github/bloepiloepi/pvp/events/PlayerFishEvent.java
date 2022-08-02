package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.projectile.FishingBobber;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerFishEvent implements PlayerEvent {

    private final Player player;
    private final FishingBobber bobber;

    private boolean cancelled;

    public PlayerFishEvent(Player player, FishingBobber bobber) {
        this.player = player;
        this.bobber = bobber;
    }

    public @NotNull Player getPlayer() {
        return player;
    }
}
