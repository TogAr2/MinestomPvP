package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;

public interface ExhaustionFeature extends CombatFeature {
	ExhaustionFeature NO_OP = (player, exhaustion) -> {};
	
	void addExhaustion(Player player, float exhaustion);
}
