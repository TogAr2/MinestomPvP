package io.github.bloepiloepi.pvp.events;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a totem prevents an entity from dying.
 */
public class TotemUseEvent implements EntityInstanceEvent, CancellableEvent {
	
	private final LivingEntity entity;
	private final Player.Hand hand;
	
	private boolean cancelled;
	
	public TotemUseEvent(@NotNull LivingEntity entity, @NotNull Player.Hand hand) {
		this.entity = entity;
		this.hand = hand;
	}
	
	@NotNull
	@Override
	public LivingEntity getEntity() {
		return entity;
	}
	
	@NotNull
	public Player.Hand getHand() {
		return hand;
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
