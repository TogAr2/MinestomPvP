package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

public interface AttackFeature extends CombatFeature {
	/**
	 * Performs an attack on the target entity.
	 *
	 * @param attacker the attacking entity
	 * @param target the target entity
	 * @return whether the attack was successful
	 */
	boolean performAttack(LivingEntity attacker, Entity target);
}
