package io.github.bloepiloepi.pvp.food;

import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FoodComponents {
	public static final Tag<NBT> SUSPICIOUS_STEW_EFFECTS = Tag.NBT("Effects");
	private static final List<FoodComponent> COMPONENTS = new ArrayList<>();
	
	public static final FoodComponent APPLE = (new FoodComponent.Builder()).hunger(4).saturationModifier(0.3F).build(Material.APPLE);
	public static final FoodComponent BAKED_POTATO = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).build(Material.BAKED_POTATO);
	public static final FoodComponent BEEF = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).meat().build(Material.BEEF);
	public static final FoodComponent BEETROOT = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.6F).build(Material.BEETROOT);
	public static final FoodComponent BEETROOT_SOUP = createSoup(6, Material.BEETROOT_SOUP);
	public static final FoodComponent BREAD = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).build(Material.BREAD);
	public static final FoodComponent CARROT = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.6F).build(Material.CARROT);
	public static final FoodComponent CHICKEN = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).statusEffect(new Potion(PotionEffect.HUNGER, (byte) 0, 600, PotionListener.defaultFlags()), 0.3F).meat().build(Material.CHICKEN);
	public static final FoodComponent CHORUS_FRUIT = createChorusFruit();
	public static final FoodComponent COD = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build(Material.COD);
	public static final FoodComponent COOKED_BEEF = (new FoodComponent.Builder()).hunger(8).saturationModifier(0.8F).meat().build(Material.COOKED_BEEF);
	public static final FoodComponent COOKED_CHICKEN = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.6F).meat().build(Material.COOKED_CHICKEN);
	public static final FoodComponent COOKED_COD = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).build(Material.COOKED_COD);
	public static final FoodComponent COOKED_MUTTON = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.8F).meat().build(Material.COOKED_MUTTON);
	public static final FoodComponent COOKED_PORKCHOP = (new FoodComponent.Builder()).hunger(8).saturationModifier(0.8F).meat().build(Material.COOKED_PORKCHOP);
	public static final FoodComponent COOKED_RABBIT = (new FoodComponent.Builder()).hunger(5).saturationModifier(0.6F).meat().build(Material.COOKED_RABBIT);
	public static final FoodComponent COOKED_SALMON = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.8F).build(Material.COOKED_SALMON);
	public static final FoodComponent COOKIE = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build(Material.COOKIE);
	public static final FoodComponent DRIED_KELP = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.3F).snack().build(Material.DRIED_KELP);
	public static final FoodComponent ENCHANTED_GOLDEN_APPLE = (new FoodComponent.Builder()).hunger(4).saturationModifier(1.2F).statusEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 400, PotionListener.defaultFlags()), 1.0F).statusEffect(new Potion(PotionEffect.RESISTANCE, (byte) 0, 6000, PotionListener.defaultFlags()), 1.0F).statusEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 6000, PotionListener.defaultFlags()), 1.0F).statusEffect(new Potion(PotionEffect.ABSORPTION, (byte) 3, 2400, PotionListener.defaultFlags()), 1.0F).alwaysEdible().build(Material.ENCHANTED_GOLDEN_APPLE);
	public static final FoodComponent GOLDEN_APPLE = (new FoodComponent.Builder()).hunger(4).saturationModifier(1.2F).statusEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 100, PotionListener.defaultFlags()), 1.0F).statusEffect(new Potion(PotionEffect.ABSORPTION, (byte) 0, 2400, PotionListener.defaultFlags()), 1.0F).alwaysEdible().build(Material.GOLDEN_APPLE);
	public static final FoodComponent GOLDEN_CARROT = (new FoodComponent.Builder()).hunger(6).saturationModifier(1.2F).build(Material.GOLDEN_CARROT);
	public static final FoodComponent HONEY_BOTTLE = (new FoodComponent.Builder()).hunger(6).saturationModifier(0.1F).drink().onEat((player, stack) -> player.removeEffect(PotionEffect.POISON)).turnsInto(Material.GLASS_BOTTLE).build(Material.HONEY_BOTTLE);
	public static final FoodComponent MELON_SLICE = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).build(Material.MELON_SLICE);
	public static final FoodComponent MUSHROOM_STEW = createSoup(6, Material.MUSHROOM_STEW);
	public static final FoodComponent MUTTON = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).meat().build(Material.MUTTON);
	public static final FoodComponent POISONOUS_POTATO = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.3F).statusEffect(new Potion(PotionEffect.POISON, (byte) 0, 100, PotionListener.defaultFlags()), 0.6F).build(Material.POISONOUS_POTATO);
	public static final FoodComponent PORKCHOP = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).meat().build(Material.PORKCHOP);
	public static final FoodComponent POTATO = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.3F).build(Material.POTATO);
	public static final FoodComponent PUFFERFISH = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.1F).statusEffect(new Potion(PotionEffect.POISON, (byte) 3, 1200, PotionListener.defaultFlags()), 1.0F).statusEffect(new Potion(PotionEffect.HUNGER, (byte) 2, 300, PotionListener.defaultFlags()), 1.0F).statusEffect(new Potion(PotionEffect.NAUSEA, (byte) 0, 300, PotionListener.defaultFlags()), 1.0F).build(Material.PUFFERFISH);
	public static final FoodComponent PUMPKIN_PIE = (new FoodComponent.Builder()).hunger(8).saturationModifier(0.3F).build(Material.PUMPKIN_PIE);
	public static final FoodComponent RABBIT = (new FoodComponent.Builder()).hunger(3).saturationModifier(0.3F).meat().build(Material.RABBIT);
	public static final FoodComponent RABBIT_STEW = createSoup(10, Material.RABBIT_STEW);
	public static final FoodComponent ROTTEN_FLESH = (new FoodComponent.Builder()).hunger(4).saturationModifier(0.1F).statusEffect(new Potion(PotionEffect.HUNGER, (byte) 0, 600, PotionListener.defaultFlags()), 0.8F).meat().build(Material.ROTTEN_FLESH);
	public static final FoodComponent SALMON = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build(Material.SALMON);
	public static final FoodComponent SPIDER_EYE = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.8F).statusEffect(new Potion(PotionEffect.POISON, (byte) 0, 100, PotionListener.defaultFlags()), 1.0F).build(Material.SPIDER_EYE);
	public static final FoodComponent SUSPICIOUS_STEW = createSuspiciousStew();
	public static final FoodComponent SWEET_BERRIES = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build(Material.SWEET_BERRIES);
	public static final FoodComponent GLOW_BERRIES = (new FoodComponent.Builder()).hunger(2).saturationModifier(0.1F).build(Material.GLOW_BERRIES);
	public static final FoodComponent TROPICAL_FISH = (new FoodComponent.Builder()).hunger(1).saturationModifier(0.1F).build(Material.TROPICAL_FISH);
	
	// Not an actual food, used as a FoodComponent here for convenience
	public static final FoodComponent MILK_BUCKET = (new FoodComponent.Builder()).alwaysEdible().drink().onEat((player, stack) -> player.clearEffects()).turnsInto(Material.BUCKET).build(Material.MILK_BUCKET);
	
	private static FoodComponent.Builder createSoupBuilder(int hunger, boolean alwaysEdible) {
		FoodComponent.Builder builder = new FoodComponent.Builder().hunger(hunger)
				.saturationModifier(0.6F).turnsInto(Material.BOWL);
		if (alwaysEdible) builder.alwaysEdible();
		return builder;
	}
	
	private static FoodComponent createSoup(int hunger, Material material) {
		return createSoupBuilder(hunger, false).build(material);
	}
	
	@SuppressWarnings("unchecked")
	private static FoodComponent createSuspiciousStew() {
		return createSoupBuilder(6, true).onEat((player, stack) -> {
			if (stack.hasTag(SUSPICIOUS_STEW_EFFECTS)) {
				NBT effectNbt = stack.getTag(SUSPICIOUS_STEW_EFFECTS);
				if (!(effectNbt instanceof NBTList nbtList)) return;
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
		}).build(Material.SUSPICIOUS_STEW);
	}
	
	private static FoodComponent createChorusFruit() {
		return new FoodComponent.Builder().hunger(4).saturationModifier(0.3F).alwaysEdible().onEat((player, stack) -> {
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
					SoundManager.sendToAround(instance, prevPosition, SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT,
							Sound.Source.PLAYER, 1.0F, 1.0F);
					
					if (!player.isSilent()) {
						SoundManager.sendToAround(player, player, SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT,
								Sound.Source.PLAYER, 1.0F, 1.0F);
					}
					
					break;
				}
			}
			
			Tracker.setCooldown(player, Material.CHORUS_FRUIT, 20);
		}).build(Material.CHORUS_FRUIT);
	}
	
	static void registerComponent(FoodComponent component) {
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
}
