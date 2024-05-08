package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;

public interface ExhaustionFeature extends CombatFeature {
	ExhaustionFeature NO_OP = new ExhaustionFeature() {
		@Override
		public void addExhaustion(Player player, float exhaustion) {}
		
		@Override
		public void addAttackExhaustion(Player player) {}
		
		@Override
		public void addDamageExhaustion(Player player, DamageType type) {}
	};
	
	void addExhaustion(Player player, float exhaustion);
	
	void addAttackExhaustion(Player player);
	
	void addDamageExhaustion(Player player, DamageType type);
}
