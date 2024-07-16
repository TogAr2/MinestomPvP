package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;

/**
 * Combat features which handles natural regeneration and starvation.
 */
public interface RegenerationFeature extends CombatFeature {
	RegenerationFeature NO_OP = (player, health, exhaustion) -> {};
	
	void regenerate(Player player, float health, float exhaustion);
}
