package io.github.bloepiloepi.pvp.events;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entities potion state (ambient, particle color and invisibility) is updated.
 */
public class PotionVisibilityEvent implements EntityInstanceEvent, CancellableEvent {
	
	private final LivingEntity entity;
	private boolean ambient;
	private int color;
	private boolean invisible;
	
	private boolean cancelled;
	
	public PotionVisibilityEvent(@NotNull LivingEntity entity, boolean ambient,
	                             int color, boolean invisible) {
		this.entity = entity;
		this.ambient = ambient;
		this.color = color;
		this.invisible = invisible;
	}
	
	@Override
	public @NotNull LivingEntity getEntity() {
		return entity;
	}
	
	/**
	 * Gets whether the entity effects contain ambient effects.
	 *
	 * @return whether the effects contain ambient effects
	 */
	public boolean isAmbient() {
		return ambient;
	}
	
	/**
	 * Sets whether the entity effects contain ambient effects.
	 *
	 * @param ambient whether the effects contain ambient effects
	 */
	public void setAmbient(boolean ambient) {
		this.ambient = ambient;
	}
	
	/**
	 * Gets the potion particle color.
	 * Will be 0 for no potion particles.
	 *
	 * @return the potion color
	 */
	public int getColor() {
		return color;
	}
	
	/**
	 * Sets the potion particle color.
	 * Set to 0 to disable potion particles.
	 *
	 * @param color the potion color
	 */
	public void setColor(int color) {
		this.color = color;
	}
	
	/**
	 * Gets whether the entity will become invisible.
	 *
	 * @return whether the entity will become invisible
	 */
	public boolean isInvisible() {
		return invisible;
	}
	
	/**
	 * Sets whether the entity will become invisible.
	 *
	 * @param invisible whether the entity will become invisible
	 */
	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
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
