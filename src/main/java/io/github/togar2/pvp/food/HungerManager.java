package io.github.togar2.pvp.food;

import io.github.togar2.pvp.config.FoodConfig;
import io.github.togar2.pvp.events.PlayerExhaustEvent;
import io.github.togar2.pvp.events.PlayerRegenerateEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.Difficulty;

public class HungerManager {
	public static final Tag<Float> EXHAUSTION = Tag.Float("exhaustion");
	public static final Tag<Integer> STARVATION_TICKS = Tag.Integer("starvationTicks");
	
	public static void add(Player player, int food, float exhaustion) {
		player.setFood(Math.min(food + player.getFood(), 20));
		player.setFoodSaturation(Math.min(player.getFoodSaturation() + (float) food * exhaustion * 2.0f, player.getFood()));
	}
	
	public static void eat(Player player, Material material) {
		FoodComponent foodComponent = FoodComponents.fromMaterial(material);
		if (foodComponent == null) return;
		add(player, foodComponent.getNutrition(), foodComponent.getSaturationModifier());
	}
	
	public static void update(Player player, FoodConfig config) {
		if (!player.getGameMode().canTakeDamage()) return;
		Difficulty difficulty = MinecraftServer.getDifficulty();
		
		if (config.isNaturalExhaustionEnabled()) {
			float exhaustion = player.getTag(EXHAUSTION);
			if (exhaustion > 4) {
				player.setTag(EXHAUSTION, exhaustion - 4);
				if (player.getFoodSaturation() > 0) {
					player.setFoodSaturation(Math.max(player.getFoodSaturation() - 1, 0));
				} else if (difficulty != Difficulty.PEACEFUL) {
					player.setFood(Math.max(player.getFood() - 1, 0));
				}
			}
		}
		
		// Natural regeneration
		if (config.isNaturalRegenerationEnabled()) {
			int starvationTicks = player.getTag(STARVATION_TICKS);
			if (!config.isLegacy() && player.getFoodSaturation() > 0 && player.getHealth() > 0
					&& player.getHealth() < player.getAttributeValue(Attribute.GENERIC_MAX_HEALTH) && player.getFood() >= 20) {
				starvationTicks++;
				if (starvationTicks >= 10) {
					float amount = Math.min(player.getFoodSaturation(), 6);
					regenerate(player, amount / 6, amount);
					starvationTicks = 0;
				}
			} else if (player.getFood() >= 18 && player.getHealth() > 0
					&& player.getHealth() < player.getAttributeValue(Attribute.GENERIC_MAX_HEALTH)) {
				starvationTicks++;
				if (starvationTicks >= 80) {
					regenerate(player, 1, config.isLegacy() ? 3 : 6);
					starvationTicks = 0;
				}
			} else if (player.getFood() <= 0) {
				starvationTicks++;
				if (starvationTicks >= 80) {
					if (player.getHealth() > 10 || difficulty == Difficulty.HARD
							|| ((player.getHealth() > 1) && (difficulty == Difficulty.NORMAL))) {
						player.damage(DamageType.STARVE, 1);
					}
					
					starvationTicks = 0;
				}
			} else {
				starvationTicks = 0;
			}
			
			player.setTag(STARVATION_TICKS, starvationTicks);
		}
	}
	
	private static void regenerate(Player player, float health, float exhaustion) {
		PlayerRegenerateEvent event = new PlayerRegenerateEvent(player, health, exhaustion);
		EventDispatcher.callCancellable(event, () -> {
			player.setHealth(player.getHealth() + event.getAmount());
			addExhaustion(player, event.getExhaustion());
		});
	}
	
	public static void addExhaustion(Player player, float exhaustion) {
		PlayerExhaustEvent playerExhaustEvent = new PlayerExhaustEvent(player, exhaustion);
		EventDispatcher.callCancellable(playerExhaustEvent, () -> player.setTag(EXHAUSTION,
				Math.min(player.getTag(EXHAUSTION) + playerExhaustEvent.getAmount(), 40)));
	}
}
