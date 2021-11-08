package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	
	public LivingEntityMixin(@NotNull EntityType entityType, @NotNull UUID uuid) {
		super(entityType, uuid);
	}
	
	@Shadow
	protected DamageType lastDamageSource;
	@Shadow public abstract void setHealth(float health);
	@Shadow public abstract float getHealth();
	@Shadow public abstract boolean isDead();
	@Shadow public abstract boolean isInvulnerable();
	@Shadow public abstract boolean isImmune(@NotNull DamageType type);
	
	/**
	 * @author me
	 * @param type the damage type
	 * @param value the damage amount
	 * @return true if the damage was successful
	 */
	@SuppressWarnings("ConstantConditions")
	@Overwrite
	public boolean damage(DamageType type, float value) {
		if (isDead())
			return false;
		if (isInvulnerable()) {
			if (type instanceof CustomDamageType) {
				if (!((CustomDamageType) type).isOutOfWorld()) return false;
			} else if (type != DamageType.VOID) {
				return false;
			}
		}
		if (isImmune(type)) {
			return false;
		}
		
		EntityDamageEvent entityDamageEvent = new EntityDamageEvent((LivingEntity) (Entity) this, type, value);
		EventDispatcher.callCancellable(entityDamageEvent, () -> {
			// Set the last damage type since the event is not cancelled
			this.lastDamageSource = entityDamageEvent.getDamageType();
			
			float remainingDamage = entityDamageEvent.getDamage();
			
			//TODO damage animation is removed since it is done using statuses,
			// but when the pvp listeners are not applied it should appear.
			// Same for the sound
			
			// Additional hearts support
			if ((Entity) this instanceof Player) {
				final Player player = (Player) (Object) this;
				final float additionalHearts = player.getAdditionalHearts();
				if (additionalHearts > 0) {
					if (remainingDamage > additionalHearts) {
						remainingDamage -= additionalHearts;
						player.setAdditionalHearts(0);
					} else {
						player.setAdditionalHearts(additionalHearts - remainingDamage);
						remainingDamage = 0;
					}
				}
			}
			
			// Set the final entity health
			float finalHealth = getHealth() - remainingDamage;
			setHealth(finalHealth);
		});
		
		return !entityDamageEvent.isCancelled();
	}
}
