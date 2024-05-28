package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;

public interface TridentFeature extends CombatFeature {
	TridentFeature NO_OP = (player, level) -> {};
	
	void applyRiptide(Player player, int level);
}
