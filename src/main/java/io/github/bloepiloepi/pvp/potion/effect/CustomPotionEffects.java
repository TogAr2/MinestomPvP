package io.github.bloepiloepi.pvp.potion.effect;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.potion.PotionEffect;

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
				new CustomPotionEffect(PotionEffect.SPEED, 8171462).addAttributeModifier(Attribute.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", (float) 0.20000000298023224D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.SLOWNESS, 5926017).addAttributeModifier(Attribute.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", (float) -0.15000000596046448D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.HASTE, 14270531).addAttributeModifier(Attribute.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", (float) 0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.MINING_FATIGUE, 4866583).addAttributeModifier(Attribute.ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", (float) -0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.STRENGTH, 9643043).addAttributeModifier(Attribute.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 3.0F, AttributeOperation.ADDITION).addLegacyAttributeModifier(Attribute.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 1.3F, AttributeOperation.MULTIPLY_TOTAL),
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
				new CustomPotionEffect(PotionEffect.WEAKNESS, 4738376).addAttributeModifier(Attribute.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -4.0F, AttributeOperation.ADDITION).addLegacyAttributeModifier(Attribute.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", -0.5F, AttributeOperation.ADDITION),
				new CustomPotionEffect(PotionEffect.POISON, 5149489),
				new CustomPotionEffect(PotionEffect.WITHER, 3484199),
				new HealthBoostPotionEffect(16284963).addAttributeModifier(Attribute.MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 4.0F, AttributeOperation.ADDITION),
				new AbsorptionPotionEffect(2445989),
				new InstantPotionEffect(PotionEffect.SATURATION, 16262179),
				new GlowingPotionEffect(9740385),
				new CustomPotionEffect(PotionEffect.LEVITATION, 13565951),
				new CustomPotionEffect(PotionEffect.LUCK, 3381504).addAttributeModifier(Attribute.LUCK, "03C3C89D-7037-4B42-869F-B146BCB64D2E", 1.0F, AttributeOperation.ADDITION),
				new CustomPotionEffect(PotionEffect.UNLUCK, 12624973).addAttributeModifier(Attribute.LUCK, "CC5AF142-2BD2-4215-B636-2605AED11727", -1.0F, AttributeOperation.ADDITION),
				new CustomPotionEffect(PotionEffect.SLOW_FALLING, 16773073),
				new CustomPotionEffect(PotionEffect.CONDUIT_POWER, 1950417),
				new CustomPotionEffect(PotionEffect.DOLPHINS_GRACE, 8954814),
				new CustomPotionEffect(PotionEffect.BAD_OMEN, 745784),
				new CustomPotionEffect(PotionEffect.HERO_OF_THE_VILLAGE, 4521796)
		);
	}
}
