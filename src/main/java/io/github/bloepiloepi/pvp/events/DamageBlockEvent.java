package io.github.bloepiloepi.pvp.events;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an entity blocks damage using a shield.
 */
public class DamageBlockEvent implements EntityEvent, CancellableEvent {
	
	private final LivingEntity entity;
	private boolean knockbackAttacker;
	
	private boolean cancelled;
	
	public DamageBlockEvent(@NotNull LivingEntity entity) {
		this.entity = entity;
		this.knockbackAttacker = false;
	}
	
	@NotNull
	@Override
	public LivingEntity getEntity() {
		return entity;
	}
	
	public boolean knockbackAttacker() {
		return knockbackAttacker;
	}
	
	/**
	 * This fixes a bug introduced in 1.14. Prior to 1.14, the attacker would receive
	 * knockback when the victim was blocking. In 1.14 and above, this is no longer the case.
	 * To apply the fix, set this to true (false by default).
	 *
	 * @param knockbackAttacker true if the attacker should be knocked back
	 */
	public void setKnockbackAttacker(boolean knockbackAttacker) {
		this.knockbackAttacker = knockbackAttacker;
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
