package io.github.togar2.pvp.food;

import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.potion.PotionListener;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.registry.Registry;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FoodComponents {
	public static final Tag<NBT> SUSPICIOUS_STEW_EFFECTS = Tag.NBT("Effects");
	private static final List<FoodComponent> COMPONENTS = new ArrayList<>();
	
	private static final FoodComponent.FoodBehaviour SOUP_BEHAVIOUR = new FoodComponent.FoodBehaviour(ItemStack.of(Material.BOWL));
	
	@SuppressWarnings("unchecked")
	private static final FoodComponent.FoodBehaviour SUSPICIOUS_STEW_BEHAVIOUR = new FoodComponent.FoodBehaviour(ItemStack.of(Material.BOWL)) {
		@Override
		public void onEat(Player player, ItemStack stack) {
			if (stack.hasTag(SUSPICIOUS_STEW_EFFECTS)) {
				NBT effectNbt = stack.getTag(SUSPICIOUS_STEW_EFFECTS);
				if (!(effectNbt instanceof NBTList<?> nbtList)) return;
				NBTList<NBTCompound> effects = (NBTList<NBTCompound>) nbtList;
				
				for (NBTCompound effectNBT : effects) {
					int duration = 160;
					if (effectNBT.containsKey("EffectDuration")) {
						Integer i = effectNBT.getAsInt("EffectDuration");
						if (i != null) duration = i;
					}
					
					Byte effectId = effectNBT.getByte("EffectId");
					if (effectId != null) {
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
						instance.getDimensionType().getMinY(), instance.getDimensionType().getMinY()
								+ instance.getDimensionType().getLogicalHeight() - 1);
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
		var registry = Registry.load(Registry.Resource.ITEMS);
		for (Map.Entry<String, Map<String, Object>> entry : registry.entrySet()) {
			Material material = Material.fromNamespaceId(entry.getKey());
			if (material != null && material.isFood()) {
				register(load(material, Registry.Properties.fromMap(entry.getValue())));
			}
		}
	}
	
	private static FoodComponent load(Material material, Registry.Properties properties) {
		SoundEvent eatingSound = SoundEvent.fromNamespaceId(properties.getString("eatingSound"));
		SoundEvent drinkingSound = SoundEvent.fromNamespaceId(properties.getString("drinkingSound"));
		
		Registry.Properties foodProperties = properties.section("foodProperties");
		boolean alwaysEdible = foodProperties.getBoolean("alwaysEdible");
		boolean isFastFood = foodProperties.getBoolean("isFastFood");
		int nutrition = foodProperties.getInt("nutrition");
		float saturationModifier = (float) foodProperties.getDouble("saturationModifier");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> serialized = (List<Map<String, Object>>) foodProperties.asMap().get("effects");
		List<FoodComponent.FoodEffect> effects = new ArrayList<>();
		
		for (Map<String, Object> map : serialized) {
			Registry.Properties effectProperties = Registry.Properties.fromMap(map);
			PotionEffect effect = PotionEffect.fromNamespaceId(effectProperties.getString("id"));
			if (effect == null) continue;
			int amplifier = effectProperties.getInt("amplifier");
			int duration = effectProperties.getInt("duration");
			double chance = effectProperties.getDouble("chance");
			effects.add(new FoodComponent.FoodEffect(new Potion(effect, (byte) amplifier, duration, PotionListener.defaultFlags()), chance));
		}
		
		if (material == Material.HONEY_BOTTLE) {
			// For some reason vanilla doesn't say it's always edible but just overrides the method to always consume it
			alwaysEdible = true;
		}
		
		return new FoodComponent(
				material, nutrition, saturationModifier, alwaysEdible,
				isFastFood, effects, eatingSound, drinkingSound,
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
