package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.LegacyKnockbackSettings;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity gets knocked back by another entity.
 * This event does not apply simply when {@code Entity.takeKnockback()} is called,
 * but only when an entity is attacked by another entity which causes the knockback.
 */
public class LegacyKnockbackEvent implements EntityEvent, CancellableEvent {
	
	private final Entity entity;
	private final Entity attacker;
	private LegacyKnockbackSettings settings;
	
	private boolean cancelled;
	
	public LegacyKnockbackEvent(@NotNull Entity entity, @NotNull Entity attacker) {
		this.entity = entity;
		this.attacker = attacker;
	}
	
	@NotNull
	@Override
	public Entity getEntity() {
		return entity;
	}
	
	/**
	 * Gets the attacker of the entity. In case of a projectile,
	 * this returns the projectile itself and not the owner.
	 *
	 * @return the attacker
	 */
	@NotNull
	public Entity getAttacker() {
		return attacker;
	}
	
	/**
	 * Gets the settings of the knockback.
	 *
	 * @return the strength
	 */
	public LegacyKnockbackSettings getSettings() {
		return settings;
	}
	
	/**
	 * Sets the settings of the knockback.
	 *
	 * @param settings the strength
	 */
	public void setStrength(LegacyKnockbackSettings settings) {
		this.settings = settings;
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
