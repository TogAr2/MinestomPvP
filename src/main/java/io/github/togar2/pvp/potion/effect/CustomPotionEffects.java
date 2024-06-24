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
		//TODO add new effects
		register(
				new CustomPotionEffect(PotionEffect.SPEED).addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, NamespaceID.from("minecraft:effect.speed"), (float) 0.20000000298023224D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.SLOWNESS).addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, NamespaceID.from("minecraft:effect.slowness"), (float) -0.15000000596046448D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.HASTE).addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, NamespaceID.from("minecraft:effect.haste"), (float) 0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.MINING_FATIGUE).addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, NamespaceID.from("minecraft:effect.mining_fatigue"), (float) -0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.STRENGTH).addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.strength"), 3.0F, AttributeOperation.ADD_VALUE).addLegacyAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.strength"), 1.3F, AttributeOperation.MULTIPLY_TOTAL),
				new CustomPotionEffect(PotionEffect.INSTANT_HEALTH),
				new CustomPotionEffect(PotionEffect.INSTANT_DAMAGE),
				new CustomPotionEffect(PotionEffect.JUMP_BOOST),
				new CustomPotionEffect(PotionEffect.NAUSEA),
				new CustomPotionEffect(PotionEffect.REGENERATION),
				new CustomPotionEffect(PotionEffect.RESISTANCE),
				new CustomPotionEffect(PotionEffect.FIRE_RESISTANCE),
				new CustomPotionEffect(PotionEffect.WATER_BREATHING),
				new CustomPotionEffect(PotionEffect.INVISIBILITY),
				new CustomPotionEffect(PotionEffect.BLINDNESS),
				new CustomPotionEffect(PotionEffect.NIGHT_VISION),
				new CustomPotionEffect(PotionEffect.HUNGER),
				new CustomPotionEffect(PotionEffect.WEAKNESS).addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.weakness"), -4.0F, AttributeOperation.ADD_VALUE).addLegacyAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.weakness"), -0.5F, AttributeOperation.ADD_VALUE),
				new CustomPotionEffect(PotionEffect.POISON),
				new CustomPotionEffect(PotionEffect.WITHER),
				new HealthBoostPotionEffect().addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, NamespaceID.from("minecraft:effect.health_boost"), 4.0F, AttributeOperation.ADD_VALUE),
				new AbsorptionPotionEffect(),
				new CustomPotionEffect(PotionEffect.SATURATION),
				new GlowingPotionEffect(),
				new CustomPotionEffect(PotionEffect.LEVITATION),
				new CustomPotionEffect(PotionEffect.LUCK).addAttributeModifier(Attribute.GENERIC_LUCK, NamespaceID.from("minecraft:effect.luck"), 1.0F, AttributeOperation.ADD_VALUE),
				new CustomPotionEffect(PotionEffect.UNLUCK).addAttributeModifier(Attribute.GENERIC_LUCK, NamespaceID.from("minecraft:effect.unluck"), -1.0F, AttributeOperation.ADD_VALUE),
				new CustomPotionEffect(PotionEffect.SLOW_FALLING),
				new CustomPotionEffect(PotionEffect.CONDUIT_POWER),
				new CustomPotionEffect(PotionEffect.DOLPHINS_GRACE),
				new CustomPotionEffect(PotionEffect.BAD_OMEN),
				new CustomPotionEffect(PotionEffect.HERO_OF_THE_VILLAGE)
		);
	}
}
