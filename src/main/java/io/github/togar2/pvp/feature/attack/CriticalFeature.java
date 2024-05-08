package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;

public interface CriticalFeature extends CombatFeature {
	CriticalFeature NO_OP = new CriticalFeature() {
		@Override
		public boolean shouldCrit(LivingEntity attacker, AttackValues.PreCritical values) {
			return false;
		}
		
		@Override
		public float applyToDamage(float damage) {
			return damage;
		}
	};
	
	boolean shouldCrit(LivingEntity attacker, AttackValues.PreCritical values);
	
	float applyToDamage(float damage);
}
