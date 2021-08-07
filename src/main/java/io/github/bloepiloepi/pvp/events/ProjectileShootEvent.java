package io.github.bloepiloepi.pvp.events;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player shoots or throws a projectile.
 * This can be a snowball, egg, ender pearl, potion, or arrow.
 * <br>
 * In case of a multishot crossbow, this event is called 3 times for each projectile.
 */
public class ProjectileShootEvent implements ItemEvent, PlayerEvent, CancellableEvent {
	
	private final Player player;
	private final ItemStack projectile;
	private boolean cancelled;
	
	public ProjectileShootEvent(@NotNull Player player, @NotNull ItemStack projectile) {
		this.player = player;
		this.projectile = projectile;
	}
	
	@Override
	public @NotNull Player getPlayer() {
		return player;
	}
	
	@Override
	public @NotNull ItemStack getItemStack() {
		return projectile;
	}
	
	public @NotNull ItemStack getProjectile() {
		return projectile;
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
