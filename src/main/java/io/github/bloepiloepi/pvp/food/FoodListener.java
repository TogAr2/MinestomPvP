package io.github.bloepiloepi.pvp.food;

import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.entity.Player;
import net.minestom.server.event.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.potion.Potion;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class FoodListener {
	
	public static EventNode<PlayerEvent> events() {
		EventNode<PlayerEvent> node = EventNode.type("food-events", EventFilter.PLAYER);
		
		node.addListener(EventListener.builder(PlayerPreEatEvent.class).handler(event -> {
			FoodComponent foodComponent = FoodComponents.fromMaterial(event.getFoodItem().getMaterial());
			
			//If no food, or if the players hunger is full and the food is not always edible, cancel
			if (foodComponent == null || (!foodComponent.isAlwaysEdible() && event.getPlayer().getFood() == 20)) {
				event.setCancelled(true);
				return;
			}
			
			event.setEatingTime(foodComponent.isSnack() ? (long) ((16 / 20F) * 1000) : (long) ((32 / 20F) * 1000));
		}).filter(event -> event.getFoodItem().getMaterial().isFood()).ignoreCancelled(false).build()); //May also be a potion
		
		node.addListener(EventListener.builder(PlayerEatEvent.class).handler(event -> {
			Tracker.hungerManager.get(event.getPlayer().getUuid()).eat(event.getFoodItem().getMaterial());
			
			FoodComponent component = FoodComponents.fromMaterial(event.getFoodItem().getMaterial());
			assert component != null;
			List<Pair<Potion, Float>> effectList = component.getStatusEffects();
			
			for (Pair<Potion, Float> pair : effectList) {
				ThreadLocalRandom random = ThreadLocalRandom.current();
				
				if (pair.first() != null && random.nextFloat() < pair.second()) {
					event.getPlayer().addEffect(pair.first());
				}
			}
			
			if (!event.getPlayer().isCreative()) {
				event.getPlayer().setItemInHand(event.getHand(), event.getFoodItem().withAmount((i) -> i - 1));
			}
		}).filter(event -> event.getFoodItem().getMaterial().isFood()).build()); //May also be a potion
		
		node.addListener(EventListener.builder(PlayerBlockBreakEvent.class).handler(event ->
				EntityUtils.addExhaustion(event.getPlayer(), 0.005F)).ignoreCancelled(false).build());
		
		node.addListener(EventListener.builder(PlayerMoveEvent.class).handler(event -> {
			Player player = event.getPlayer();
			
			double xDiff = event.getNewPosition().getX() - player.getPosition().getX();
			double yDiff = event.getNewPosition().getY() - player.getPosition().getY();
			double zDiff = event.getNewPosition().getZ() - player.getPosition().getZ();
			
			//Check if movement was a jump
			if (yDiff > 0.0D && player.isOnGround()) {
				if (player.isSprinting()) {
					EntityUtils.addExhaustion(player, 0.2F);
				} else {
					EntityUtils.addExhaustion(player, 0.05F);
				}
			}
			
			if (player.isOnGround()) {
				int l = (int) Math.round(Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 100.0F);
				if (l > 0) {
					EntityUtils.addExhaustion(player, (player.isSprinting() ? 0.1F : 0.0F) * (float) l * 0.01F);
				}
			} else {
				if (Objects.requireNonNull(player.getInstance()).getBlock(player.getPosition().toBlockPosition()) == Block.WATER) {
					int l = (int) Math.round(Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff) * 100.0F);
					if (l > 0) {
						EntityUtils.addExhaustion(player, 0.01F * (float) l * 0.01F);
					}
				}
			}
		}).ignoreCancelled(false).build());
		
		return node;
	}
}
