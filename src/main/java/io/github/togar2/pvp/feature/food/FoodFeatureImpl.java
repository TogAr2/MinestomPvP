package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.feature.IndependentFeature;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.food.FoodComponent;
import io.github.togar2.pvp.food.FoodComponents;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class FoodFeatureImpl implements FoodFeature, RegistrableFeature, IndependentFeature {
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(PlayerPreEatEvent.class, event -> {
			if (event.getItemStack().material() != Material.MILK_BUCKET
					&& !event.getItemStack().material().isFood())
				return;
			
			FoodComponent foodComponent = FoodComponents.fromMaterial(event.getItemStack().material());
			
			// If no food, or if the players hunger is full and the food is not always edible, cancel
			if (foodComponent == null || (!event.getPlayer().isCreative()
					&& !foodComponent.isAlwaysEdible() && event.getPlayer().getFood() == 20)) {
				event.setCancelled(true);
				return;
			}
			
			event.setEatingTime((long) getUseTime(foodComponent) * MinecraftServer.TICK_MS);
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			if (event.getItemStack().material() != Material.MILK_BUCKET
					&& !event.getItemStack().material().isFood())
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
		this.eat(player, stack.material());
		
		FoodComponent component = FoodComponents.fromMaterial(stack.material());
		assert component != null;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		triggerEatingSound(player, component);
		
		if (stack.material() != Material.MILK_BUCKET) {
			ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
					SoundEvent.ENTITY_PLAYER_BURP, Sound.Source.PLAYER,
					0.5f, random.nextFloat() * 0.1f + 0.9f
			), player);
		}
		
		List<FoodComponent.FoodEffect> effectList = component.getFoodEffects();
		
		for (FoodComponent.FoodEffect effect : effectList) {
			if (random.nextFloat() < effect.chance()) {
				player.addEffect(effect.potion());
			}
		}
		
		if (component.getBehaviour() != null) component.getBehaviour().onEat(player, stack);
		
		if (!player.isCreative()) {
			ItemStack leftOver = component.getBehaviour() != null ? component.getBehaviour().getLeftOver() : null;
			if (leftOver != null) {
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
	public void eat(Player player, Material material) {
		FoodComponent foodComponent = FoodComponents.fromMaterial(material);
		if (foodComponent == null) return;
		addFood(player, foodComponent.getNutrition(), foodComponent.getSaturationModifier());
	}
	
	public static void tickEatingSounds(Player player) {
		ItemStack stack = player.getItemInHand(Objects.requireNonNull(player.getEatingHand()));
		
		FoodComponent component = FoodComponents.fromMaterial(stack.material());
		if (component == null) return;
		
		long useTime = getUseTime(component);
		long usedDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
		long usedTicks = usedDuration / MinecraftServer.TICK_MS;
		long remainingUseTicks = useTime - usedTicks;
		
		boolean canTrigger = component.isSnack() || remainingUseTicks <= useTime - 7;
		boolean shouldTrigger = canTrigger && remainingUseTicks % 4 == 0;
		if (!shouldTrigger) return;
		
		triggerEatingSound(player, component);
	}
	
	public static void triggerEatingSound(Player player, @Nullable FoodComponent component) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		if (component == null || component.isDrink()) { // null = potion
			SoundEvent soundEvent = component != null ? component.getDrinkingSound() : SoundEvent.ENTITY_GENERIC_DRINK;
			player.getViewersAsAudience().playSound(Sound.sound(
					soundEvent, Sound.Source.PLAYER,
					0.5f, random.nextFloat() * 0.1f + 0.9f
			));
		} else {
			player.getViewersAsAudience().playSound(Sound.sound(
					component.getEatingSound(), Sound.Source.PLAYER,
					0.5f + 0.5f * random.nextInt(2),
					(random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f
			));
		}
	}
	
	private static int getUseTime(@NotNull FoodComponent foodComponent) {
		if (foodComponent.getMaterial() == Material.HONEY_BOTTLE) return 40;
		return foodComponent.isSnack() ? 16 : 32;
	}
}
