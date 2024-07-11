package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public interface FoodFeature extends CombatFeature {
	FoodFeature NO_OP = new FoodFeature() {
		@Override
		public void addFood(Player player, int food, float exhaustion) {}
		
		@Override
		public void eat(Player player, ItemStack stack) {}
		
		@Override
		public void applySaturationEffect(Player player, byte amplifier) {}
	};
	
	void addFood(Player player, int food, float exhaustion);
	
	void eat(Player player, ItemStack stack);
	
	void applySaturationEffect(Player player, byte amplifier);
}
