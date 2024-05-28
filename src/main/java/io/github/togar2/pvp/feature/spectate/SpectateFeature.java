package io.github.togar2.pvp.feature.spectate;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

public interface SpectateFeature extends CombatFeature {
	SpectateFeature NO_OP = new SpectateFeature() {
		@Override
		public void makeSpectate(Player player, Entity target) {}
		
		@Override
		public void stopSpectating(Player player) {}
	};
	
	void makeSpectate(Player player, Entity target);
	
	void stopSpectating(Player player);
}
