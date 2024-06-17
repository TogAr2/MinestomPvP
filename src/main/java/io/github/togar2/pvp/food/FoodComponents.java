package io.github.togar2.pvp.food;

import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.potion.PotionListener;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Food;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FoodComponents {
	public static final Tag<BinaryTag> SUSPICIOUS_STEW_EFFECTS = Tag.NBT("Effects");
	private static final List<FoodComponent> COMPONENTS = new ArrayList<>();
	
	private static final FoodComponent.FoodBehaviour SOUP_BEHAVIOUR = new FoodComponent.FoodBehaviour(ItemStack.of(Material.BOWL));
	
	@SuppressWarnings("unchecked")
	private static final FoodComponent.FoodBehaviour SUSPICIOUS_STEW_BEHAVIOUR = new FoodComponent.FoodBehaviour(ItemStack.of(Material.BOWL)) {
		@Override
		public void onEat(Player player, ItemStack stack) {
			if (stack.hasTag(SUSPICIOUS_STEW_EFFECTS)) {
				BinaryTag effectNbt = stack.getTag(SUSPICIOUS_STEW_EFFECTS);
				if (!(effectNbt instanceof ListBinaryTag nbtList)) return;

				for (BinaryTag tag : nbtList) {
					CompoundBinaryTag effectNBT = (CompoundBinaryTag) tag;

					int duration = 160;
					if (effectNBT.keySet().contains("EffectDuration")) {
						int i = effectNBT.getInt("EffectDuration");
						duration = i;
					}
					
					if (effectNBT.keySet().contains("EffectId")) {
						byte effectId = effectNBT.getByte("EffectId");

						PotionEffect potionEffect = PotionEffect.fromId(effectId);
						if (potionEffect != null) {
							player.addEffect(new Potion(potionEffect, (byte) 0, duration, PotionListener.defaultFlags()));
						}
					}
				}
			}
		}
	};
	
	private static final FoodComponent.FoodBehaviour CHORUS_FRUIT_BEHAVIOUR = new FoodComponent.FoodBehaviour(null) {
		@Override
		public void onEat(Player player, ItemStack stack) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			Instance instance = player.getInstance();
			DimensionType instanceDimension = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
			assert instance != null;
			
			Pos prevPosition = player.getPosition();
			double prevX = prevPosition.x();
			double prevY = prevPosition.y();
			double prevZ = prevPosition.z();
			
			float pitch = prevPosition.pitch();
			float yaw = prevPosition.yaw();
			
			// Max 16 tries
			for (int i = 0; i < 16; i++) {
				double x = prevX + (random.nextDouble() - 0.5) * 16.0;
				double y = MathUtils.clamp(prevY + (random.nextInt(16) - 8),
						instanceDimension.minY(), instanceDimension.minY()
								+ instanceDimension.logicalHeight() - 1);
				double z = prevZ + (random.nextDouble() - 0.5) * 16.0;
				
				if (player.getVehicle() != null) {
					player.getVehicle().removePassenger(player);
				}
				
				if (EntityUtils.randomTeleport(player, new Pos(x, y, z, yaw, pitch), true)) {
					ViewUtil.packetGroup(player).playSound(Sound.sound(
							SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT, Sound.Source.PLAYER,
							1.0f, 1.0f
					), prevPosition);
					
					if (!player.isSilent()) {
						player.getViewersAsAudience().playSound(Sound.sound(
								SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT, Sound.Source.PLAYER,
								1.0f, 1.0f
						), player);
					}
					
					break;
				}
			}
			
			Tracker.setCooldown(player, Material.CHORUS_FRUIT, 20);
		}
	};
	
	public static void register(FoodComponent component) {
		COMPONENTS.add(component);
	}
	
	public static FoodComponent fromMaterial(Material material) {
		for (FoodComponent component : COMPONENTS) {
			if (component.getMaterial() == material) {
				return component;
			}
		}
		
		return null;
	}
	
	public static void registerAll() {
		loadFromRegistry();
		
		// Here to simplify listener code
		register(new FoodComponent(
				Material.MILK_BUCKET, 0, 0.0f,
				true, false, List.of(),
				SoundEvent.ENTITY_GENERIC_DRINK, SoundEvent.ENTITY_GENERIC_DRINK,
				getBehaviour(Material.MILK_BUCKET)
		));
	}
	
	public static void loadFromRegistry() {
		for (Material items : Material.values()) {
			if (items.prototype().has(ItemComponent.FOOD)) {
				register(load(items, items.prototype().get(ItemComponent.FOOD)));
			}
		}
	}
	
	private static FoodComponent load(Material material, Food food) {
		boolean alwaysEdible = food.canAlwaysEat();
		boolean isFastFood = food.eatSeconds() == 0.8F;
		int nutrition = food.nutrition();
		float saturationModifier = food.saturationModifier();

		List<FoodComponent.FoodEffect> effects = new ArrayList<>();

		for (Food.EffectChance effect : food.effects()) {
			effects.add(
					new FoodComponent.FoodEffect(
							new Potion(
									effect.effect().id(),
									effect.effect().amplifier(),
									effect.effect().duration()
							),
							effect.probability()
					)
			);
		}
		
		if (material == Material.HONEY_BOTTLE) {
			// For some reason vanilla doesn't say it's always edible but just overrides the method to always consume it
			alwaysEdible = true;
		}
		
		return new FoodComponent(
				material, nutrition, saturationModifier, alwaysEdible,
				isFastFood, effects, SoundEvent.ENTITY_GENERIC_EAT, SoundEvent.ENTITY_GENERIC_DRINK,
				getBehaviour(material)
		);
	}
	
	private static @Nullable FoodComponent.FoodBehaviour getBehaviour(Material material) {
		if (material == Material.MILK_BUCKET) {
			return new FoodComponent.FoodBehaviour(ItemStack.of(Material.BUCKET)) {
				@Override
				public void onEat(Player player, ItemStack stack) {
					player.clearEffects();
				}
			};
		} else if (material == Material.HONEY_BOTTLE) {
			return new FoodComponent.FoodBehaviour(ItemStack.of(Material.GLASS_BOTTLE)) {
				@Override
				public void onEat(Player player, ItemStack stack) {
					player.removeEffect(PotionEffect.POISON);
				}
			};
		} else if (material == Material.CHORUS_FRUIT) {
			return CHORUS_FRUIT_BEHAVIOUR;
		} else if (material == Material.BEETROOT_SOUP || material == Material.MUSHROOM_STEW || material == Material.RABBIT_STEW) {
			return SOUP_BEHAVIOUR;
		} else if (material == Material.SUSPICIOUS_STEW) {
			return SUSPICIOUS_STEW_BEHAVIOUR;
		} else {
			return null;
		}
	}
}
