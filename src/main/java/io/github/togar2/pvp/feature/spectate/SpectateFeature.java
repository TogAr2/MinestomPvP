package io.github.togar2.pvp.feature.spectate;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public interface SpectateFeature extends CombatFeature {
	void makeSpectate(Player player, Entity target);
	
	void stopSpectating(Player player);
}
