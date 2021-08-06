package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.projectile.EntityHittableProjectile;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an {@link io.github.bloepiloepi.pvp.projectile.EntityHittableProjectile} hits an entity or a block.
 * <br>
 * Note: this event is called BEFORE the actual hit (when in front of the hit position).
 */
public abstract class ProjectileHitEvent implements EntityEvent {
	
	private final EntityHittableProjectile projectile;
	
	public ProjectileHitEvent(@NotNull EntityHittableProjectile projectile) {
		this.projectile = projectile;
	}
	
	@Override
	public @NotNull EntityHittableProjectile getEntity() {
		return projectile;
	}
	
	/**
	 * Called when a {@link io.github.bloepiloepi.pvp.projectile.EntityHittableProjectile} hits a block.
	 * <br>
	 * Note: this event is called BEFORE the actual hit (when in front of the hit position).
	 */
	public static class ProjectileBlockHitEvent extends ProjectileHitEvent {
		
		private final Position position;
		
		public ProjectileBlockHitEvent(@NotNull EntityHittableProjectile projectile, @NotNull Position position) {
			super(projectile);
			this.position = position;
		}
		
		public Position getPosition() {
			return position;
		}
	}
	
	/**
	 * Called when a {@link io.github.bloepiloepi.pvp.projectile.EntityHittableProjectile} hits an entity.
	 * Cancelling will make the projectile go through (the event will be called a few more times in this case).
	 * <br>
	 * Note: this event is called BEFORE the actual hit (when in front of the hit position).
	 */
	public static class ProjectileEntityHitEvent extends ProjectileHitEvent implements CancellableEvent {
		
		private final Entity hitEntity;
		private boolean cancelled;
		
		public ProjectileEntityHitEvent(@NotNull EntityHittableProjectile projectile, @NotNull Entity hitEntity) {
			super(projectile);
			this.hitEntity = hitEntity;
		}
		
		public Entity getHitEntity() {
			return hitEntity;
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
}
