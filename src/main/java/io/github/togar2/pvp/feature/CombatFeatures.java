package io.github.togar2.pvp.feature;

import io.github.togar2.pvp.feature.armor.VanillaArmorFeature;
import io.github.togar2.pvp.feature.attack.VanillaAttackFeature;
import io.github.togar2.pvp.feature.attack.VanillaCriticalFeature;
import io.github.togar2.pvp.feature.attack.VanillaSweepingFeature;
import io.github.togar2.pvp.feature.attributes.VanillaEquipmentFeature;
import io.github.togar2.pvp.feature.block.LegacyVanillaBlockFeature;
import io.github.togar2.pvp.feature.block.VanillaBlockFeature;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.cooldown.VanillaAttackCooldownFeature;
import io.github.togar2.pvp.feature.cooldown.VanillaItemCooldownFeature;
import io.github.togar2.pvp.feature.damage.VanillaDamageFeature;
import io.github.togar2.pvp.feature.effect.VanillaEffectFeature;
import io.github.togar2.pvp.feature.enchantment.VanillaEnchantmentFeature;
import io.github.togar2.pvp.feature.explosion.VanillaExplosionFeature;
import io.github.togar2.pvp.feature.fall.VanillaFallFeature;
import io.github.togar2.pvp.feature.food.VanillaExhaustionFeature;
import io.github.togar2.pvp.feature.food.VanillaFoodFeature;
import io.github.togar2.pvp.feature.food.VanillaRegenerationFeature;
import io.github.togar2.pvp.feature.item.VanillaItemDamageFeature;
import io.github.togar2.pvp.feature.knockback.VanillaKnockbackFeature;
import io.github.togar2.pvp.feature.potion.VanillaPotionFeature;
import io.github.togar2.pvp.feature.projectile.*;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.feature.spectate.VanillaSpectateFeature;
import io.github.togar2.pvp.feature.state.VanillaPlayerStateFeature;
import io.github.togar2.pvp.feature.totem.VanillaTotemFeature;
import io.github.togar2.pvp.feature.tracking.VanillaDeathMessageFeature;
import io.github.togar2.pvp.utils.CombatVersion;

import java.util.List;

public class CombatFeatures {
	public static final DefinedFeature<VanillaArmorFeature> VANILLA_ARMOR = VanillaArmorFeature.DEFINED;
	public static final DefinedFeature<VanillaAttackFeature> VANILLA_ATTACK = VanillaAttackFeature.DEFINED;
	public static final DefinedFeature<VanillaCriticalFeature> VANILLA_CRITICAL = VanillaCriticalFeature.DEFINED;
	public static final DefinedFeature<VanillaSweepingFeature> VANILLA_SWEEPING = VanillaSweepingFeature.DEFINED;
	public static final DefinedFeature<VanillaEquipmentFeature> VANILLA_EQUIPMENT = VanillaEquipmentFeature.DEFINED;
	public static final DefinedFeature<VanillaBlockFeature> VANILLA_BLOCK = VanillaBlockFeature.DEFINED;
	public static final DefinedFeature<VanillaAttackCooldownFeature> VANILLA_ATTACK_COOLDOWN = VanillaAttackCooldownFeature.DEFINED;
	public static final DefinedFeature<VanillaItemCooldownFeature> VANILLA_ITEM_COOLDOWN = VanillaItemCooldownFeature.DEFINED;
	public static final DefinedFeature<VanillaDamageFeature> VANILLA_DAMAGE = VanillaDamageFeature.DEFINED;
	public static final DefinedFeature<VanillaEffectFeature> VANILLA_EFFECT = VanillaEffectFeature.DEFINED;
	public static final DefinedFeature<VanillaEnchantmentFeature> VANILLA_ENCHANTMENT = VanillaEnchantmentFeature.DEFINED;
	public static final DefinedFeature<VanillaExplosionFeature> VANILLA_EXPLOSION = VanillaExplosionFeature.DEFINED;
	public static final DefinedFeature<VanillaFallFeature> VANILLA_FALL = VanillaFallFeature.DEFINED;
	public static final DefinedFeature<VanillaExhaustionFeature> VANILLA_EXHAUSTION = VanillaExhaustionFeature.DEFINED;
	public static final DefinedFeature<VanillaFoodFeature> VANILLA_FOOD = VanillaFoodFeature.DEFINED;
	public static final DefinedFeature<VanillaRegenerationFeature> VANILLA_REGENERATION = VanillaRegenerationFeature.DEFINED;
	public static final DefinedFeature<VanillaItemDamageFeature> VANILLA_ITEM_DAMAGE = VanillaItemDamageFeature.DEFINED;
	public static final DefinedFeature<VanillaKnockbackFeature> VANILLA_KNOCKBACK = VanillaKnockbackFeature.DEFINED;
	public static final DefinedFeature<VanillaPotionFeature> VANILLA_POTION = VanillaPotionFeature.DEFINED;
	public static final DefinedFeature<VanillaBowFeature> VANILLA_BOW = VanillaBowFeature.DEFINED;
	public static final DefinedFeature<VanillaCrossbowFeature> VANILLA_CROSSBOW = VanillaCrossbowFeature.DEFINED;
	public static final DefinedFeature<VanillaFishingRodFeature> VANILLA_FISHING_ROD = VanillaFishingRodFeature.DEFINED;
	public static final DefinedFeature<VanillaItemProjectileFeature> VANILLA_ITEM_PROJECTILE = VanillaItemProjectileFeature.DEFINED;
	public static final DefinedFeature<VanillaTridentFeature> VANILLA_TRIDENT = VanillaTridentFeature.DEFINED;
	public static final DefinedFeature<VanillaSpectateFeature> VANILLA_SPECTATE = VanillaSpectateFeature.DEFINED;
	public static final DefinedFeature<VanillaPlayerStateFeature> VANILLA_PLAYER_STATE = VanillaPlayerStateFeature.DEFINED;
	public static final DefinedFeature<VanillaTotemFeature> VANILLA_TOTEM = VanillaTotemFeature.DEFINED;
	public static final DefinedFeature<VanillaDeathMessageFeature> VANILLA_DEATH_MESSAGE = VanillaDeathMessageFeature.DEFINED;
	
	public static final DefinedFeature<LegacyVanillaBlockFeature> LEGACY_VANILLA_BLOCK = LegacyVanillaBlockFeature.SHIELD;
	
	private static final List<DefinedFeature<?>> VANILLA = List.of(
			VANILLA_ARMOR, VANILLA_ATTACK, VANILLA_CRITICAL, VANILLA_SWEEPING,
			VANILLA_EQUIPMENT, VANILLA_BLOCK, VANILLA_ATTACK_COOLDOWN, VANILLA_ITEM_COOLDOWN,
			VANILLA_DAMAGE, VANILLA_EFFECT, VANILLA_ENCHANTMENT, VANILLA_EXPLOSION,
			VANILLA_FALL, VANILLA_EXHAUSTION, VANILLA_FOOD, VANILLA_REGENERATION,
			VANILLA_ITEM_DAMAGE, VANILLA_KNOCKBACK, VANILLA_POTION, VANILLA_BOW,
			VANILLA_CROSSBOW, VANILLA_FISHING_ROD, VANILLA_ITEM_PROJECTILE, VANILLA_TRIDENT,
			VANILLA_SPECTATE, VANILLA_PLAYER_STATE, VANILLA_TOTEM, VANILLA_DEATH_MESSAGE
	);
	
	public static final CombatFeatureSet MODERN_VANILLA = getVanilla(CombatVersion.MODERN, DifficultyProvider.DEFAULT).build();
	
	public static final CombatFeatureSet LEGACY_VANILLA = getVanilla(CombatVersion.LEGACY, DifficultyProvider.DEFAULT)
			.add(LEGACY_VANILLA_BLOCK)
			.build();
	
	public static CombatConfiguration getVanilla(CombatVersion version, DifficultyProvider difficultyProvider) {
		return new CombatConfiguration()
				.version(version).difficulty(difficultyProvider)
				.addAll(VANILLA);
	}
}
