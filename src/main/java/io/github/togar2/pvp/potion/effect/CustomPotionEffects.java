package io.github.togar2.pvp.potion.effect;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.NamespaceID;

import java.util.HashMap;
import java.util.Map;

public class CustomPotionEffects {
	private static final Map<PotionEffect, CombatPotionEffect> POTION_EFFECTS = new HashMap<>();
	
	public static CombatPotionEffect get(PotionEffect potionEffect) {
		return POTION_EFFECTS.get(potionEffect);
	}
	
	public static void register(CombatPotionEffect... potionEffects) {
		for (CombatPotionEffect potionEffect : potionEffects) {
			POTION_EFFECTS.put(potionEffect.getPotionEffect(), potionEffect);
		}
	}
	
	public static void registerAll() {
		//TODO add new effects
		register(
				new CombatPotionEffect(PotionEffect.SPEED).addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, NamespaceID.from("minecraft:effect.speed"), (float) 0.20000000298023224D, AttributeOperation.MULTIPLY_TOTAL),
				new CombatPotionEffect(PotionEffect.SLOWNESS).addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, NamespaceID.from("minecraft:effect.slowness"), (float) -0.15000000596046448D, AttributeOperation.MULTIPLY_TOTAL),
				new CombatPotionEffect(PotionEffect.HASTE).addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, NamespaceID.from("minecraft:effect.haste"), (float) 0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CombatPotionEffect(PotionEffect.MINING_FATIGUE).addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, NamespaceID.from("minecraft:effect.mining_fatigue"), (float) -0.10000000149011612D, AttributeOperation.MULTIPLY_TOTAL),
				new CombatPotionEffect(PotionEffect.STRENGTH).addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.strength"), 3.0F, AttributeOperation.ADD_VALUE).addLegacyAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.strength"), 1.3F, AttributeOperation.MULTIPLY_TOTAL),
				new CombatPotionEffect(PotionEffect.INSTANT_HEALTH),
				new CombatPotionEffect(PotionEffect.INSTANT_DAMAGE),
				new CombatPotionEffect(PotionEffect.JUMP_BOOST),
				new CombatPotionEffect(PotionEffect.NAUSEA),
				new CombatPotionEffect(PotionEffect.REGENERATION),
				new CombatPotionEffect(PotionEffect.RESISTANCE),
				new CombatPotionEffect(PotionEffect.FIRE_RESISTANCE),
				new CombatPotionEffect(PotionEffect.WATER_BREATHING),
				new CombatPotionEffect(PotionEffect.INVISIBILITY),
				new CombatPotionEffect(PotionEffect.BLINDNESS),
				new CombatPotionEffect(PotionEffect.NIGHT_VISION),
				new CombatPotionEffect(PotionEffect.HUNGER),
				new CombatPotionEffect(PotionEffect.WEAKNESS).addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.weakness"), -4.0F, AttributeOperation.ADD_VALUE).addLegacyAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, NamespaceID.from("minecraft:effect.weakness"), -0.5F, AttributeOperation.ADD_VALUE),
				new CombatPotionEffect(PotionEffect.POISON),
				new CombatPotionEffect(PotionEffect.WITHER),
				new HealthBoostPotionEffect().addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, NamespaceID.from("minecraft:effect.health_boost"), 4.0F, AttributeOperation.ADD_VALUE),
				new AbsorptionPotionEffect(),
				new CombatPotionEffect(PotionEffect.SATURATION),
				new GlowingPotionEffect(),
				new CombatPotionEffect(PotionEffect.LEVITATION),
				new CombatPotionEffect(PotionEffect.LUCK).addAttributeModifier(Attribute.GENERIC_LUCK, NamespaceID.from("minecraft:effect.luck"), 1.0F, AttributeOperation.ADD_VALUE),
				new CombatPotionEffect(PotionEffect.UNLUCK).addAttributeModifier(Attribute.GENERIC_LUCK, NamespaceID.from("minecraft:effect.unluck"), -1.0F, AttributeOperation.ADD_VALUE),
				new CombatPotionEffect(PotionEffect.SLOW_FALLING),
				new CombatPotionEffect(PotionEffect.CONDUIT_POWER),
				new CombatPotionEffect(PotionEffect.DOLPHINS_GRACE),
				new CombatPotionEffect(PotionEffect.BAD_OMEN),
				new CombatPotionEffect(PotionEffect.HERO_OF_THE_VILLAGE)
		);
	}
}
