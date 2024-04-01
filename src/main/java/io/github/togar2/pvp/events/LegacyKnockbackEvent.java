package io.github.togar2.pvp.events;

import io.github.togar2.pvp.legacy.LegacyKnockbackSettings;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity gets knocked back by another entity.
 * This event does not apply simply when {@code Entity.takeKnockback()} is called,
 * but only when an entity is attacked by another entity which causes the knockback.
 * <br><br>
 * You should be aware that when the attacker has a knockback weapon, this event will be called twice:
 * once for the default damage knockback, once for for the extra knockback.
 * You can determine which knockback this is by using {@code isExtraKnockback()}.
 */
public class LegacyKnockbackEvent implements EntityInstanceEvent, CancellableEvent {
	
	private final Entity entity;
	private final Entity attacker;
	private final boolean extraKnockback;
	private LegacyKnockbackSettings settings = LegacyKnockbackSettings.DEFAULT;
	
	private boolean cancelled;
	
	public LegacyKnockbackEvent(@NotNull Entity entity, @NotNull Entity attacker,
	                            boolean extraKnockback) {
		this.entity = entity;
		this.attacker = attacker;
		this.extraKnockback = extraKnockback;
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
	 * Gets whether this knockback is the extra knockback
	 * caused by a knockback enchanted weapon.
	 *
	 * @return true if it is, false otherwise
	 */
	public boolean isExtraKnockback() {
		return extraKnockback;
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
	public void setSettings(LegacyKnockbackSettings settings) {
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
