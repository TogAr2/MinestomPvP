package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class FinalDamageEvent implements EntityEvent, CancellableEvent {
	
	private final LivingEntity entity;
	private final CustomDamageType damageType;
	private float damage;
	
	private boolean cancelled;
	
	public FinalDamageEvent(@NotNull LivingEntity entity, @NotNull CustomDamageType damageType, float damage) {
		this.entity = entity;
		this.damageType = damageType;
		this.damage = damage;
	}
	
	@NotNull
	@Override
	public LivingEntity getEntity() {
		return entity;
	}
	
	/**
	 * Gets the damage type.
	 *
	 * @return the damage type
	 */
	@NotNull
	public CustomDamageType getDamageType() {
		return damageType;
	}
	
	/**
	 * Gets the damage amount.
	 *
	 * @return the damage amount
	 */
	public float getDamage() {
		return damage;
	}
	
	/**
	 * Changes the damage amount.
	 *
	 * @param damage the new damage amount
	 */
	public void setDamage(float damage) {
		this.damage = damage;
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
