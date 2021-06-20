package io.github.bloepiloepi.pvp.food;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;

import java.util.List;

public class FoodComponent {
	private final int hunger;
	private final float saturationModifier;
	private final boolean meat;
	private final boolean alwaysEdible;
	private final boolean snack;
	private final List<Pair<Potion, Float>> statusEffects;
	private final Material material;
	
	private FoodComponent(int hunger, float saturationModifier, boolean meat, boolean alwaysEdible, boolean snack, List<it.unimi.dsi.fastutil.Pair<Potion, Float>> statusEffects, Material material) {
		this.hunger = hunger;
		this.saturationModifier = saturationModifier;
		this.meat = meat;
		this.alwaysEdible = alwaysEdible;
		this.snack = snack;
		this.statusEffects = statusEffects;
		this.material = material;
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
	
	public List<it.unimi.dsi.fastutil.Pair<Potion, Float>> getStatusEffects() {
		return this.statusEffects;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public static class Builder {
		private int hunger;
		private float saturationModifier;
		private boolean meat;
		private boolean alwaysEdible;
		private boolean snack;
		private final List<it.unimi.dsi.fastutil.Pair<Potion, Float>> statusEffects = Lists.newArrayList();
		
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
		
		public FoodComponent.Builder statusEffect(Potion effect, float chance) {
			this.statusEffects.add(Pair.of(effect, chance));
			return this;
		}
		
		public FoodComponent build(Material material) {
			FoodComponent component = new FoodComponent(this.hunger, this.saturationModifier, this.meat, this.alwaysEdible, this.snack, this.statusEffects, material);
			FoodComponents.registerComponent(component);
			return component;
		}
	}
}
