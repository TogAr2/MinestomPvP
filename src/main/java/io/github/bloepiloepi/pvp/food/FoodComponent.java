package io.github.bloepiloepi.pvp.food;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;

import java.util.List;
import java.util.function.BiConsumer;

public class FoodComponent {
	private final int hunger;
	private final float saturationModifier;
	private final boolean meat;
	private final boolean alwaysEdible;
	private final boolean snack;
	private final boolean drink;
	private final List<Pair<Potion, Float>> statusEffects;
	private final Material material;
	private final ItemStack turnsInto;
	private final BiConsumer<Player, ItemStack> onEat;
	
	private FoodComponent(int hunger, float saturationModifier, boolean meat, boolean alwaysEdible,
	                      boolean snack, boolean drink, List<Pair<Potion, Float>> statusEffects,
	                      Material material, Material turnsInto, BiConsumer<Player, ItemStack> onEat) {
		this.hunger = hunger;
		this.saturationModifier = saturationModifier;
		this.meat = meat;
		this.alwaysEdible = alwaysEdible;
		this.snack = snack;
		this.drink = drink;
		this.statusEffects = statusEffects;
		this.material = material;
		this.turnsInto = turnsInto == null ? ItemStack.AIR : ItemStack.of(turnsInto);
		this.onEat = onEat;
	}
	
	public int getHunger() {
		return this.hunger;
	}
	
	public float getSaturationModifier() {
		return this.saturationModifier;
	}
	
	public boolean isMeat() {
		return this.meat;
	}
	
	public boolean isAlwaysEdible() {
		return this.alwaysEdible;
	}
	
	public boolean isSnack() {
		return this.snack;
	}
	
	public boolean isDrink() {
		return this.drink;
	}
	
	public List<Pair<Potion, Float>> getStatusEffects() {
		return this.statusEffects;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public boolean hasTurnsInto() {
		return !turnsInto.isAir();
	}
	
	public ItemStack getTurnsInto() {
		return turnsInto;
	}
	
	public void onEat(Player player, ItemStack stack) {
		if (onEat != null) {
			onEat.accept(player, stack);
		}
	}
	
	public static class Builder {
		private int hunger;
		private float saturationModifier;
		private boolean meat;
		private boolean alwaysEdible;
		private boolean snack;
		private boolean drink;
		private Material turnsInto;
		private BiConsumer<Player, ItemStack> onEat;
		private final List<Pair<Potion, Float>> statusEffects = Lists.newArrayList();
		
		public FoodComponent.Builder hunger(int hunger) {
			this.hunger = hunger;
			return this;
		}
		
		public FoodComponent.Builder saturationModifier(float saturationModifier) {
			this.saturationModifier = saturationModifier;
			return this;
		}
		
		public FoodComponent.Builder meat() {
			this.meat = true;
			return this;
		}
		
		public FoodComponent.Builder alwaysEdible() {
			this.alwaysEdible = true;
			return this;
		}
		
		public FoodComponent.Builder snack() {
			this.snack = true;
			return this;
		}
		
		public FoodComponent.Builder drink() {
			this.drink = true;
			return this;
		}
		
		public FoodComponent.Builder statusEffect(Potion effect, float chance) {
			this.statusEffects.add(Pair.of(effect, chance));
			return this;
		}
		
		public FoodComponent.Builder turnsInto(Material turnsInto) {
			this.turnsInto = turnsInto;
			return this;
		}
		
		public FoodComponent.Builder onEat(BiConsumer<Player, ItemStack> onEat) {
			this.onEat = onEat;
			return this;
		}
		
		public FoodComponent build(Material material) {
			FoodComponent component = new FoodComponent(hunger, saturationModifier, meat,
					alwaysEdible, snack, drink, statusEffects, material, turnsInto, onEat);
			FoodComponents.registerComponent(component);
			return component;
		}
	}
}
