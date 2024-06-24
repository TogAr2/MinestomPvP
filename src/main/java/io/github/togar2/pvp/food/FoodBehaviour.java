package io.github.togar2.pvp.food;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FoodBehaviour {
	private final ItemStack convertsTo;
	
	public FoodBehaviour(@NotNull ItemStack convertsTo) {
		this.convertsTo = convertsTo;
	}
	
	public void onEat(Player player, ItemStack stack) {
	}
	
	public @NotNull ItemStack getConvertsTo() {
		return convertsTo;
	}
}
