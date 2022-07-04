package io.github.bloepiloepi.pvp.events;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a death message for the respawn screen of a player is requested.
 */
public class DeathScreenEvent implements EntityInstanceEvent, PlayerEvent, CancellableEvent {
	
	private final Player player;
	private Component message;
	
	private boolean cancelled;
	
	public DeathScreenEvent(@NotNull Player player, @Nullable Component message) {
		this.player = player;
		this.message = message;
	}
	
	@Override
	public @NotNull Player getPlayer() {
		return player;
	}
	
	/**
	 * Gets the death message.
	 *
	 * @return the death message
	 */
	public @Nullable Component getMessage() {
		return message;
	}
	
	/**
	 * Sets the death message.
	 *
	 * @param message the death message
	 */
	public void setMessage(@Nullable Component message) {
		this.message = message;
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
