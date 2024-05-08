package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;

public interface CriticalFeature extends CombatFeature {
	boolean shouldCrit(LivingEntity attacker, AttackValues.PreCritical values);
	
	float applyToDamage(float damage);
}
