package io.github.togar2.pvp.food;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FoodComponent {
	private final Material material;
	private final int nutrition;
	private final float saturationModifier;
	private final boolean alwaysEdible;
	private final boolean snack;
	private final List<FoodEffect> effects;
	
	private final SoundEvent eatingSound;
	private final SoundEvent drinkingSound;
	private final FoodBehaviour behaviour;
	
	public FoodComponent(Material material, int nutrition, float saturationModifier,
	                      boolean alwaysEdible, boolean snack,
	                      List<FoodEffect> effects, SoundEvent eatingSound,
	                      SoundEvent drinkingSound, @Nullable FoodBehaviour behaviour) {
		this.material = material;
		this.nutrition = nutrition;
		this.saturationModifier = saturationModifier;
		this.alwaysEdible = alwaysEdible;
		this.snack = snack;
		this.effects = effects;
		this.eatingSound = eatingSound;
		this.drinkingSound = drinkingSound;
		this.behaviour = behaviour;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public int getNutrition() {
		return this.nutrition;
	}
	
	public float getSaturationModifier() {
		return this.saturationModifier;
	}
	
	public boolean isAlwaysEdible() {
		return this.alwaysEdible;
	}
	
	public boolean isSnack() {
		return this.snack;
	}
	
	public boolean isDrink() {
		return material == Material.HONEY_BOTTLE || material == Material.MILK_BUCKET;
	}
	
	public List<FoodEffect> getFoodEffects() {
		return this.effects;
	}
	
	public SoundEvent getDrinkingSound() {
		return drinkingSound;
	}
	
	public SoundEvent getEatingSound() {
		return eatingSound;
	}
	
	public FoodBehaviour getBehaviour() {
		return behaviour;
	}
	
	public record FoodEffect(Potion potion, double chance) {}
	
	public static class FoodBehaviour {
		private final ItemStack leftOver;
		
		public FoodBehaviour(@Nullable ItemStack leftOver) {
			this.leftOver = leftOver;
		}
		
		public void onEat(Player player, ItemStack stack) {}
		
		public @Nullable ItemStack getLeftOver() {
			return leftOver;
		}
	}
}
