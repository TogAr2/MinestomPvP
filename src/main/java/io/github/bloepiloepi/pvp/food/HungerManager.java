package io.github.bloepiloepi.pvp.food;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.world.Difficulty;

public class HungerManager {
	private final Player player;
	private float exhaustion;
	private int foodStarvationTimer;
	private int prevFoodLevel = 20;
	
	public HungerManager(Player player) {
		this.player = player;
	}
	
	public void add(int food, float f) {
		player.setFood(Math.min(food + player.getFood(), 20));
		player.setFoodSaturation(Math.min(player.getFoodSaturation() + (float)food * f * 2.0F, player.getFood()));
	}
	
	public void eat(Material material) {
		if (material.isFood()) {
			FoodComponent foodComponent = FoodComponents.fromMaterial(material);
			assert foodComponent != null;
			
			this.add(foodComponent.getHunger(), foodComponent.getSaturationModifier());
		}
	}
	
	public void update(boolean legacy) {
		if (!player.getGameMode().canTakeDamage()) return;
		
		Difficulty difficulty = MinecraftServer.getDifficulty();
		this.prevFoodLevel = player.getFood();
		if (this.exhaustion > 4.0F) {
			this.exhaustion -= 4.0F;
			if (player.getFoodSaturation() > 0.0F) {
				player.setFoodSaturation(Math.max(player.getFoodSaturation() - 1.0F, 0.0F));
			} else if (difficulty != Difficulty.PEACEFUL) {
				player.setFood(Math.max(player.getFood() - 1, 0));
			}
		}
		
		//Natural regeneration
		if (!legacy && player.getFoodSaturation() > 0.0F && player.getHealth() > 0.0F && player.getHealth() < player.getMaxHealth() && player.getFood() >= 20) {
			++this.foodStarvationTimer;
			if (this.foodStarvationTimer >= 10) {
				float f = Math.min(player.getFoodSaturation(), 6.0F);
				player.setHealth(player.getHealth() + (f / 6.0F));
				this.addExhaustion(f);
				this.foodStarvationTimer = 0;
			}
		} else if (player.getFood() >= 18 && player.getHealth() > 0.0F && player.getHealth() < player.getMaxHealth()) {
			++this.foodStarvationTimer;
			if (this.foodStarvationTimer >= 80) {
				player.setHealth(player.getHealth() + 1.0F);
				this.addExhaustion(legacy ? 3.0F : 6.0F);
				this.foodStarvationTimer = 0;
			}
		} else if (player.getFood() <= 0) {
			++this.foodStarvationTimer;
			if (this.foodStarvationTimer >= 80) {
				if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
					player.damage(CustomDamageType.STARVE, 1.0F);
				}
				
				this.foodStarvationTimer = 0;
			}
		} else {
			this.foodStarvationTimer = 0;
		}
	}
	
	public void addExhaustion(float exhaustion) {
		this.exhaustion = Math.min(this.exhaustion + exhaustion, 40.0F);
	}
}
