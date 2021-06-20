package io.github.bloepiloepi.pvp.events;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity blocks damage using a shield.
 */
public class DamageBlockEvent implements EntityEvent, CancellableEvent {
	
	private final LivingEntity entity;
	
	private boolean cancelled;
	
	public DamageBlockEvent(@NotNull LivingEntity entity) {
		this.entity = entity;
	}
	
	@NotNull
	@Override
	public LivingEntity getEntity() {
		return entity;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
