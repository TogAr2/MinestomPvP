package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;

/**
 * Combat feature used to determine whether an attack is a sweeping attack and also used for applying the sweeping.
 */
public interface SweepingFeature extends CombatFeature {
	SweepingFeature NO_OP = new SweepingFeature() {
		@Override
		public boolean shouldSweep(LivingEntity attacker, AttackValues.PreSweeping values) {
			return false;
		}
		
		@Override
		public float getSweepingDamage(LivingEntity attacker, float damage) {
			return 0;
		}
		
		@Override
		public void applySweeping(LivingEntity attacker, LivingEntity target, float damage) {}
	};
	
	boolean shouldSweep(LivingEntity attacker, AttackValues.PreSweeping values);
	
	float getSweepingDamage(LivingEntity attacker, float damage);
	
	void applySweeping(LivingEntity attacker, LivingEntity target, float damage);
}
