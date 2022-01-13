package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A cancellable form of the death event, which includes .
 */
public class EntityPreDeathEvent implements EntityInstanceEvent, CancellableEvent {
	
	private final Entity entity;
	private final CustomDamageType damageType;
	
	private boolean cancelled;
	
	public EntityPreDeathEvent(@NotNull Entity entity, @NotNull CustomDamageType damageType) {
		this.entity = entity;
		this.damageType = damageType;
	}
	
	@Override
	public @NotNull Entity getEntity() {
		return entity;
	}
	
	public @NotNull CustomDamageType getDamageType() {
		return damageType;
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
