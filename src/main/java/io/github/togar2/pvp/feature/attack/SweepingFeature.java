package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;

public interface SweepingFeature extends CombatFeature {
	boolean shouldSweep(LivingEntity attacker, AttackValues.PreSweeping values);
	
	float getSweepingDamage(LivingEntity attacker, float damage);
	
	void applySweeping(LivingEntity attacker, LivingEntity target, float damage);
}
