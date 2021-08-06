package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.projectile.AbstractArrow;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class PickupArrowEvent implements EntityEvent, CancellableEvent {
	
	private final LivingEntity livingEntity;
	private final AbstractArrow arrowEntity;
	
	private boolean cancelled;
	
	public PickupArrowEvent(@NotNull LivingEntity livingEntity, @NotNull AbstractArrow arrowEntity) {
		this.livingEntity = livingEntity;
		this.arrowEntity = arrowEntity;
	}
	
	@NotNull
	public LivingEntity getLivingEntity() {
		return livingEntity;
	}
	
	@NotNull
	public AbstractArrow getArrowEntity() {
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
		return livingEntity;
	}
}
