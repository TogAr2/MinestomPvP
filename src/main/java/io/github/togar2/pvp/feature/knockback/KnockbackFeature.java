package io.github.togar2.pvp.feature.knockback;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;

public interface KnockbackFeature extends CombatFeature {
	/**
	 * Apply base knockback to the target entity.
	 *
	 * @param damage the damage that caused the knockback
	 * @param target the entity that is receiving the knockback
	 * @return true if the target entity was knocked back, false otherwise
	 */
	boolean applyDamageKnockback(Damage damage, LivingEntity target);
	
	/**
	 * Applies an extra attack knockback to the target entity.
	 *
	 * @param attacker the attacker that caused the knockback
	 * @param target the entity that is receiving the knockback
	 * @return true if the target entity was knocked back, false otherwise
	 */
	boolean applyAttackKnockback(LivingEntity attacker, LivingEntity target, int knockback);
	
	boolean applySweepingKnockback(LivingEntity attacker, LivingEntity target);
}
