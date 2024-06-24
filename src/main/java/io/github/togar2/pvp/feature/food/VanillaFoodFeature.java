package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.food.FoodBehaviour;
import io.github.togar2.pvp.food.FoodBehaviours;
import io.github.togar2.pvp.utils.PotionFlags;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Food;
import net.minestom.server.item.component.SuspiciousStewEffects;
import net.minestom.server.potion.Potion;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaFoodFeature implements FoodFeature, CombatFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaFoodFeature> DEFINED = new DefinedFeature<>(
			FeatureType.FOOD, configuration -> new VanillaFoodFeature()
	);
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerPreEatEvent.class, event -> {
			if (event.getItemStack().material() != Material.MILK_BUCKET
					&& !event.getItemStack().has(ItemComponent.FOOD))
				return;
			
			Food foodComponent = event.getItemStack().get(ItemComponent.FOOD);
			
			if (foodComponent == null) {
				event.setCancelled(true);
				return;
			}
			
			// If the players hunger is full and the food is not always edible, cancel
			// For some reason vanilla doesn't say honey is always edible but just overrides the method to always consume it
			boolean alwaysEat = foodComponent.canAlwaysEat() || event.getItemStack().material() == Material.HONEY_BOTTLE;
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE
					&& !alwaysEat && event.getPlayer().getFood() == 20) {
				event.setCancelled(true);
				return;
			}
			
			event.setEatingTime(getUseTime(event.getItemStack().material(), foodComponent));
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			if (event.getItemStack().material() != Material.MILK_BUCKET
					&& !event.getItemStack().has(ItemComponent.FOOD))
				return;
			
			if (!event.getPlayer().isEating()) return; // Temporary hack, waiting on Minestom PR #2128
			
			onFinishEating(event.getPlayer(), event.getItemStack(), event.getHand());
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (player.isSilent() || !player.isEating()) return;
			
			tickEatingSounds(player);
		});
	}
	
	protected void onFinishEating(Player player, ItemStack stack, Player.Hand hand) {
		this.eat(player, stack);
		
		Food component = stack.get(ItemComponent.FOOD);
		assert component != null;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		triggerEatingSound(player, stack.material());
		
		if (stack.material() != Material.MILK_BUCKET) {
			ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
					SoundEvent.ENTITY_PLAYER_BURP, Sound.Source.PLAYER,
					0.5f, random.nextFloat() * 0.1f + 0.9f
			), player);
		}
		
		List<Food.EffectChance> effectList = component.effects();
		
		for (Food.EffectChance effect : effectList) {
			if (random.nextFloat() < effect.probability()) {
				player.addEffect(new Potion(
						effect.effect().id(), effect.effect().amplifier(),
						PotionFlags.create(
								effect.effect().isAmbient(),
								effect.effect().showParticles(),
								effect.effect().showIcon()
						)
				));
			}
		}
		
		if (stack.has(ItemComponent.SUSPICIOUS_STEW_EFFECTS)) {
			SuspiciousStewEffects effects = stack.get(ItemComponent.SUSPICIOUS_STEW_EFFECTS);
			assert effects != null;
			for (SuspiciousStewEffects.Effect effect : effects.effects()) {
				player.addEffect(new Potion(effect.id(), (byte) 0, effect.durationTicks(), PotionFlags.defaultFlags()));
			}
		}
		
		ItemStack leftOver = component.usingConvertsTo();
		
		FoodBehaviour behaviour = FoodBehaviours.fromMaterial(stack.material());
		if (behaviour != null) {
			behaviour.onEat(player, stack);
			if (leftOver.isAir()) leftOver = behaviour.getConvertsTo();
		}
		
		if (player.getGameMode() != GameMode.CREATIVE) {
			if (!leftOver.isAir()) {
				if (stack.amount() == 1) {
					player.setItemInHand(hand, leftOver);
				} else {
					player.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
					player.getInventory().addItemStack(leftOver);
				}
			} else {
				player.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
			}
		}
	}
	
	@Override
	public void addFood(Player player, int food, float exhaustion) {
		player.setFood(Math.min(food + player.getFood(), 20));
		player.setFoodSaturation(Math.min(player.getFoodSaturation() + (float) food * exhaustion * 2.0f, player.getFood()));
	}
	
	@Override
	public void eat(Player player, ItemStack stack) {
		Food foodComponent = stack.get(ItemComponent.FOOD);
		if (foodComponent == null) return;
		addFood(player, foodComponent.nutrition(), foodComponent.saturationModifier());
	}
	
	protected void tickEatingSounds(Player player) {
		ItemStack stack = player.getItemInHand(Objects.requireNonNull(player.getItemUseHand()));
		
		Food component = stack.get(ItemComponent.FOOD);
		if (component == null) return;
		
		long useTime = getUseTime(stack.material(), component);
		long usedDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
		long usedTicks = usedDuration / MinecraftServer.TICK_MS;
		long remainingUseTicks = useTime - usedTicks;
		
		boolean canTrigger = component.eatDurationTicks() < 32 || remainingUseTicks <= useTime - 7;
		boolean shouldTrigger = canTrigger && remainingUseTicks % 4 == 0;
		if (!shouldTrigger) return;
		
		triggerEatingSound(player, stack.material());
	}
	
	protected void triggerEatingSound(Player player, Material material) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		if (material == Material.HONEY_BOTTLE || material == Material.MILK_BUCKET) { // Drinking
			SoundEvent soundEvent = material == Material.HONEY_BOTTLE ?
					SoundEvent.ITEM_HONEY_BOTTLE_DRINK : SoundEvent.ENTITY_GENERIC_DRINK;
			player.getViewersAsAudience().playSound(Sound.sound(
					soundEvent, Sound.Source.PLAYER,
					0.5f, random.nextFloat() * 0.1f + 0.9f
			));
		} else { // Eating
			player.getViewersAsAudience().playSound(Sound.sound(
					SoundEvent.ENTITY_GENERIC_EAT, Sound.Source.PLAYER,
					0.5f + 0.5f * random.nextInt(2),
					(random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f
			));
		}
	}
	
	private static int getUseTime(@NotNull Material material, @NotNull Food foodComponent) {
		if (material == Material.HONEY_BOTTLE) return 40;
		return foodComponent.eatDurationTicks();
	}
}
