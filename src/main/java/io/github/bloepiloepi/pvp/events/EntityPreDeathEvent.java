package io.github.bloepiloepi.pvp.events;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A cancellable form of the death event, which includes damage type.
 * Can be used to cancel the death while still applying after-damage effects, such as attack sounds.
 * See #setCancelDeath for more information.
 */
public class EntityPreDeathEvent implements EntityInstanceEvent, CancellableEvent {
	
	private final Entity entity;
	private final CustomDamageType damageType;
	
	private boolean cancelled;
	private boolean cancelDeath;
	
	public EntityPreDeathEvent(@NotNull Entity entity, @NotNull CustomDamageType damageType) {
		this.entity = entity;
		this.damageType = damageType;
	}
	
	@Override
	public @NotNull Entity getEntity() {
		return entity;
	}
	
	public @NotNull CustomDamageType getDamageType() {
		return damageType;
	}
	
	/**
	 * Returns whether the damage should be cancelled or not.
	 * <br>
	 * WARNING: Cancelling can have unintended side effects, see #setCancelled for explanation.
	 *
	 * @return whether
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * Use this method to cancel the damage.
	 * <br>
	 * WARNING: This will also get rid of any effects applied after the damage, such as attack sounds.
	 * You might want to use #setCancelDeath instead.
	 *
	 * @param cancel true if the event should be cancelled, false otherwise
	 */
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
	
	/**
	 * See #setCancelDeath for explanation
	 *
	 * @return whether the death should be cancelled or not
	 */
	public boolean isCancelDeath() {
		return cancelDeath;
	}
	
	/**
	 * Use this method to cancel the death, but make the damage method return true.
	 * This will make sure any effects applied after the damage, such as attack sounds, still apply.
	 *
	 * @param cancelDeath whether the death should be cancelled or not
	 */
	public void setCancelDeath(boolean cancelDeath) {
		this.cancelDeath = cancelDeath;
	}
}
