package io.github.togar2.pvp.feature.block;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public interface LegacyBlockFeature extends CombatFeature {
	LegacyBlockFeature NO_OP = new LegacyBlockFeature() {
		@Override
		public boolean isBlocking(Player player) {
			return false;
		}
		
		@Override
		public void block(Player player) {}
		
		@Override
		public void unblock(Player player) {}
		
		@Override
		public boolean canBlockWith(Player player, ItemStack stack) {
			return false;
		}
	};
	
	boolean isBlocking(Player player);
	
	void block(Player player);
	
	void unblock(Player player);
	
	boolean canBlockWith(Player player, ItemStack stack);
}
