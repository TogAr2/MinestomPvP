package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.projectile.AbstractArrow;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player picks up an arrow.
 */
public class PickupArrowEvent implements EntityInstanceEvent, CancellableEvent {
	
	private final Player player;
	private final AbstractArrow arrowEntity;
	
	private boolean cancelled;
	
	public PickupArrowEvent(@NotNull Player player, @NotNull AbstractArrow arrowEntity) {
		this.player = player;
		this.arrowEntity = arrowEntity;
	}
	
	@NotNull
	public LivingEntity getPlayer() {
		return player;
	}
	
	@NotNull
	public AbstractArrow getArrow() {
		return arrowEntity;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	@Override
	public @NotNull Entity getEntity() {
		return player;
	}
}
