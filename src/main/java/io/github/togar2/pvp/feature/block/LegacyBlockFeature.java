package io.github.togar2.pvp.feature.block;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public interface LegacyBlockFeature extends BlockFeature {
	boolean isBlocking(Player player);
	
	void block(Player player);
	
	void unblock(Player player);
	
	boolean canBlockWith(Player player, ItemStack stack);
}
