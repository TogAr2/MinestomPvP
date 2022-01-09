package io.github.bloepiloepi.pvp.potion.item;

import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

public class CustomPotionTypes {
	private static final Map<PotionType, CustomPotionType> POTION_EFFECTS = new HashMap<>();
	
	public static CustomPotionType get(PotionType potionType) {
		return POTION_EFFECTS.get(potionType);
	}
	
	public static void register(CustomPotionType... potionTypes) {
		for (CustomPotionType potionType : potionTypes) {
			POTION_EFFECTS.put(potionType.getPotionType(), potionType);
		}
	}
	
	public static void registerAll() {
		register(
				new CustomPotionType(PotionType.EMPTY),
				new CustomPotionType(PotionType.WATER),
				new CustomPotionType(PotionType.MUNDANE),
				new CustomPotionType(PotionType.THICK),
				new CustomPotionType(PotionType.AWKWARD),
				
				new CustomPotionType(PotionType.NIGHT_VISION, new Potion(PotionEffect.NIGHT_VISION, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_NIGHT_VISION, new Potion(PotionEffect.NIGHT_VISION, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.INVISIBILITY, new Potion(PotionEffect.INVISIBILITY, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_INVISIBILITY, new Potion(PotionEffect.INVISIBILITY, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LEAPING, new Potion(PotionEffect.JUMP_BOOST, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_LEAPING, new Potion(PotionEffect.JUMP_BOOST, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_LEAPING, new Potion(PotionEffect.JUMP_BOOST, (byte) 1, 1800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.FIRE_RESISTANCE, new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_FIRE_RESISTANCE, new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.SWIFTNESS, new Potion(PotionEffect.SPEED, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_SWIFTNESS, new Potion(PotionEffect.SPEED, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_SWIFTNESS, new Potion(PotionEffect.SPEED, (byte) 1, 1800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.SLOWNESS, new Potion(PotionEffect.SLOWNESS, (byte) 0, 1800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_SLOWNESS, new Potion(PotionEffect.SLOWNESS, (byte) 0, 4800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_SLOWNESS, new Potion(PotionEffect.SLOWNESS, (byte) 3, 400, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.TURTLE_MASTER, new Potion(PotionEffect.SLOWNESS, (byte) 3, 400, PotionListener.defaultFlags()), new Potion(PotionEffect.RESISTANCE, (byte) 2, 400, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_TURTLE_MASTER, new Potion(PotionEffect.SLOWNESS, (byte) 3, 800, PotionListener.defaultFlags()), new Potion(PotionEffect.RESISTANCE, (byte) 2, 800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_TURTLE_MASTER, new Potion(PotionEffect.SLOWNESS, (byte) 5, 400, PotionListener.defaultFlags()), new Potion(PotionEffect.RESISTANCE, (byte) 3, 400, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.WATER_BREATHING, new Potion(PotionEffect.WATER_BREATHING, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_WATER_BREATHING, new Potion(PotionEffect.WATER_BREATHING, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.HEALING, new Potion(PotionEffect.INSTANT_HEALTH, (byte) 0, 1, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_HEALING, new Potion(PotionEffect.INSTANT_HEALTH, (byte) 1, 1, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.HARMING, new Potion(PotionEffect.INSTANT_DAMAGE, (byte) 0, 1, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_HARMING, new Potion(PotionEffect.INSTANT_DAMAGE, (byte) 1, 1, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.POISON, new Potion(PotionEffect.POISON, (byte) 0, 900, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_POISON, new Potion(PotionEffect.POISON, (byte) 0, 1800, PotionListener.defaultFlags())).legacy(new Potion(PotionEffect.POISON, (byte) 0, 2400, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_POISON, new Potion(PotionEffect.POISON, (byte) 1, 432, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.REGENERATION, new Potion(PotionEffect.REGENERATION, (byte) 0, 900, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_REGENERATION, new Potion(PotionEffect.REGENERATION, (byte) 0, 1800, PotionListener.defaultFlags())).legacy(new Potion(PotionEffect.REGENERATION, (byte) 0, 2400, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_REGENERATION, new Potion(PotionEffect.REGENERATION, (byte) 1, 450, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRENGTH, new Potion(PotionEffect.STRENGTH, (byte) 0, 3600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_STRENGTH, new Potion(PotionEffect.STRENGTH, (byte) 0, 9600, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.STRONG_STRENGTH, new Potion(PotionEffect.STRENGTH, (byte) 1, 1800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.WEAKNESS, new Potion(PotionEffect.WEAKNESS, (byte) 0, 1800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_WEAKNESS, new Potion(PotionEffect.WEAKNESS, (byte) 0, 4800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LUCK, new Potion(PotionEffect.LUCK, (byte) 0, 6000, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.SLOW_FALLING, new Potion(PotionEffect.SLOW_FALLING, (byte) 0, 1800, PotionListener.defaultFlags())),
				new CustomPotionType(PotionType.LONG_SLOW_FALLING, new Potion(PotionEffect.SLOW_FALLING, (byte) 0, 4800, PotionListener.defaultFlags()))
		);
	}
}
