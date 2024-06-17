package io.github.togar2.pvp.potion.effect;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.NamespaceID;

import java.util.HashMap;
import java.util.Map;

public class CustomPotionEffects {
	private static final Map<PotionEffect, CustomPotionEffect> POTION_EFFECTS = new HashMap<>();
	
	public static CustomPotionEffect get(PotionEffect potionEffect) {
		return POTION_EFFECTS.get(potionEffect);
	}
	
	public static void register(CustomPotionEffect... potionEffects) {
		for (CustomPotionEffect potionEffect : potionEffects) {
			POTION_EFFECTS.put(potionEffect.getPotionEffect(), potionEffect);
		}
	}
	
	public static void registerAll() {
		register(
				new CustomPotionEffect(PotionEffect.SPEED, 8171462).addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, NamespaceID.from("minecraft:effect.speed"), (float) 0.20000000298023224D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.SLOWNESS, 5926017).addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, NamespaceID.from("minecraft:effect.slowness"), (float) -0.15000000596046448D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.HASTE, 14270531).addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, NamespaceID.from("minecraft:effect.haste"), (float) 0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.MINING_FATIGUE, 4866583).addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, NamespaceID.from("minecraft:effect.mining_fatigue"), (float) -0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.STRENGTH, 9643043).addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.strength"), 3.0F, AttributeOperation.ADD_VALUE).addLegacyAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.strength"), 1.3F, AttributeOperation.MULTIPLY_TOTAL),
				new InstantPotionEffect(PotionEffect.INSTANT_HEALTH, 16262179),
				new InstantPotionEffect(PotionEffect.INSTANT_DAMAGE, 4393481),
				new CustomPotionEffect(PotionEffect.JUMP_BOOST, 2293580),
				new CustomPotionEffect(PotionEffect.NAUSEA, 5578058),
				new CustomPotionEffect(PotionEffect.REGENERATION, 13458603),
				new CustomPotionEffect(PotionEffect.RESISTANCE, 10044730),
				new CustomPotionEffect(PotionEffect.FIRE_RESISTANCE, 14981690),
				new CustomPotionEffect(PotionEffect.WATER_BREATHING, 3035801),
				new CustomPotionEffect(PotionEffect.INVISIBILITY, 8356754),
				new CustomPotionEffect(PotionEffect.BLINDNESS, 2039587),
				new CustomPotionEffect(PotionEffect.NIGHT_VISION, 2039713),
				new CustomPotionEffect(PotionEffect.HUNGER, 5797459),
				new CustomPotionEffect(PotionEffect.WEAKNESS, 4738376).addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.weakness"), -4.0F, AttributeOperation.ADD_VALUE).addLegacyAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.weakness"), -0.5F, AttributeOperation.ADD_VALUE),
				new CustomPotionEffect(PotionEffect.POISON, 5149489),
				new CustomPotionEffect(PotionEffect.WITHER, 3484199),
				new HealthBoostPotionEffect(16284963).addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, NamespaceID.from("minecraft:effect.health_boost"), 4.0F, AttributeOperation.ADD_VALUE),
				new AbsorptionPotionEffect(2445989),
				new InstantPotionEffect(PotionEffect.SATURATION, 16262179),
				new GlowingPotionEffect(9740385),
				new CustomPotionEffect(PotionEffect.LEVITATION, 13565951),
				new CustomPotionEffect(PotionEffect.LUCK, 3381504).addAttributeModifier(Attribute.GENERIC_LUCK, NamespaceID.from("minecraft:effect.luck"), 1.0F, AttributeOperation.ADD_VALUE),
				new CustomPotionEffect(PotionEffect.UNLUCK, 12624973).addAttributeModifier(Attribute.GENERIC_LUCK, NamespaceID.from("minecraft:effect.unluck"), -1.0F, AttributeOperation.ADD_VALUE),
				new CustomPotionEffect(PotionEffect.SLOW_FALLING, 16773073),
				new CustomPotionEffect(PotionEffect.CONDUIT_POWER, 1950417),
				new CustomPotionEffect(PotionEffect.DOLPHINS_GRACE, 8954814),
				new CustomPotionEffect(PotionEffect.BAD_OMEN, 745784),
				new CustomPotionEffect(PotionEffect.HERO_OF_THE_VILLAGE, 4521796)
		);
	}
}
