package io.github.togar2.pvp.feature;

import io.github.togar2.pvp.feature.armor.VanillaArmorFeature;
import io.github.togar2.pvp.feature.attack.VanillaAttackFeature;
import io.github.togar2.pvp.feature.attack.VanillaCriticalFeature;
import io.github.togar2.pvp.feature.attack.VanillaSweepingFeature;
import io.github.togar2.pvp.feature.attributes.VanillaEquipmentFeature;
import io.github.togar2.pvp.feature.block.VanillaBlockFeature;
import io.github.togar2.pvp.feature.cooldown.VanillaCooldownFeature;
import io.github.togar2.pvp.feature.damage.VanillaDamageFeature;
import io.github.togar2.pvp.feature.effect.VanillaEffectFeature;
import io.github.togar2.pvp.feature.fall.VanillaFallFeature;
import io.github.togar2.pvp.feature.food.VanillaExhaustionFeature;
import io.github.togar2.pvp.feature.food.VanillaFoodFeature;
import io.github.togar2.pvp.feature.food.VanillaRegenerationFeature;
import io.github.togar2.pvp.feature.item.VanillaItemDamageFeature;
import io.github.togar2.pvp.feature.knockback.VanillaKnockbackFeature;
import io.github.togar2.pvp.feature.potion.VanillaPotionFeature;
import io.github.togar2.pvp.feature.projectile.VanillaBowFeature;
import io.github.togar2.pvp.feature.projectile.VanillaCrossbowFeature;
import io.github.togar2.pvp.feature.projectile.VanillaFishingRodFeature;
import io.github.togar2.pvp.feature.projectile.VanillaItemProjectileFeature;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.feature.spectate.VanillaSpectateFeature;
import io.github.togar2.pvp.feature.totem.VanillaTotemFeature;
import io.github.togar2.pvp.feature.tracking.VanillaDeathMessageFeature;
import io.github.togar2.pvp.utils.CombatVersion;

import java.util.List;

public class CombatFeatures {
	private static final List<Class<? extends CombatFeature>> VANILLA = List.of(
			VanillaArmorFeature.class, VanillaAttackFeature.class, VanillaCriticalFeature.class,
			VanillaSweepingFeature.class, VanillaEquipmentFeature.class, VanillaBlockFeature.class,
			VanillaCooldownFeature.class, VanillaDamageFeature.class, VanillaEffectFeature.class,
			VanillaFallFeature.class, VanillaExhaustionFeature.class, VanillaFoodFeature.class,
			VanillaRegenerationFeature.class, VanillaItemDamageFeature.class, VanillaKnockbackFeature.class,
			VanillaPotionFeature.class, VanillaBowFeature.class, VanillaCrossbowFeature.class,
			VanillaFishingRodFeature.class, VanillaItemProjectileFeature.class, VanillaSpectateFeature.class,
			VanillaTotemFeature.class, VanillaDeathMessageFeature.class
	);
	
	public static final RegistrableFeature MODERN_VANILLA = getVanilla(CombatVersion.MODERN, DifficultyProvider.DEFAULT);
	
	public static final RegistrableFeature LEGACY_VANILLA = getVanilla(CombatVersion.LEGACY, DifficultyProvider.DEFAULT);
	
	public static RegistrableFeature getVanilla(CombatVersion version, DifficultyProvider difficultyProvider) {
		return new CombatConfiguration()
				.add(version).add(difficultyProvider)
				.addAll(VANILLA)
				.build();
	}
}
