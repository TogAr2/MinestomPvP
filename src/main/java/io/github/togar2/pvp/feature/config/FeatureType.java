package io.github.togar2.pvp.feature.config;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.armor.ArmorFeature;
import io.github.togar2.pvp.feature.attack.AttackFeature;
import io.github.togar2.pvp.feature.attack.CriticalFeature;
import io.github.togar2.pvp.feature.attack.SweepingFeature;
import io.github.togar2.pvp.feature.attributes.DataFeature;
import io.github.togar2.pvp.feature.block.BlockFeature;
import io.github.togar2.pvp.feature.block.LegacyBlockFeature;
import io.github.togar2.pvp.feature.cooldown.CooldownFeature;
import io.github.togar2.pvp.feature.damage.DamageFeature;
import io.github.togar2.pvp.feature.effect.EffectFeature;
import io.github.togar2.pvp.feature.explosion.ExplosionFeature;
import io.github.togar2.pvp.feature.fall.FallFeature;
import io.github.togar2.pvp.feature.food.ExhaustionFeature;
import io.github.togar2.pvp.feature.food.FoodFeature;
import io.github.togar2.pvp.feature.food.RegenerationFeature;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.feature.knockback.KnockbackFeature;
import io.github.togar2.pvp.feature.potion.PotionFeature;
import io.github.togar2.pvp.feature.projectile.*;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.feature.spectate.SpectateFeature;
import io.github.togar2.pvp.feature.totem.TotemFeature;
import io.github.togar2.pvp.feature.tracking.DeathMessageFeature;
import io.github.togar2.pvp.feature.tracking.TrackingFeature;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.attribute.Attribute;

public record FeatureType<F extends CombatFeature>(String name, F noopFeature) {
	public static final FeatureType<CombatVersion> VERSION = of("VERSION", CombatVersion.MODERN);
	public static final FeatureType<ArmorFeature> ARMOR = of("ARMOR", ArmorFeature.NO_OP);
	public static final FeatureType<AttackFeature> ATTACK = of("ATTACK", AttackFeature.NO_OP);
	public static final FeatureType<CriticalFeature> CRITICAL = of("CRITICAL", CriticalFeature.NO_OP);
	public static final FeatureType<SweepingFeature> SWEEPING = of("SWEEPING", SweepingFeature.NO_OP);
	public static final FeatureType<DataFeature<Attribute>> EQUIPMENT_DATA = of("EQUIPMENT_DATA", DataFeature.NO_OP);
	public static final FeatureType<BlockFeature> BLOCK = of("BLOCK", BlockFeature.NO_OP);
	public static final FeatureType<LegacyBlockFeature> LEGACY_BLOCK = of("LEGACY_BLOCK", LegacyBlockFeature.NO_OP);
	public static final FeatureType<CooldownFeature> COOLDOWN = of("COOLDOWN", CooldownFeature.NO_OP);
	public static final FeatureType<DamageFeature> DAMAGE = of("DAMAGE", DamageFeature.NO_OP);
	public static final FeatureType<EffectFeature> EFFECT = of("EFFECT", EffectFeature.NO_OP);
	public static final FeatureType<ExplosionFeature> EXPLOSION = of("EXPLOSION", ExplosionFeature.NO_OP);
	public static final FeatureType<FallFeature> FALL = of("FALL", FallFeature.NO_OP);
	public static final FeatureType<ExhaustionFeature> EXHAUSTION = of("EXHAUSTION", ExhaustionFeature.NO_OP);
	public static final FeatureType<FoodFeature> FOOD = of("FOOD", FoodFeature.NO_OP);
	public static final FeatureType<RegenerationFeature> REGENERATION = of("REGENERATION", RegenerationFeature.NO_OP);
	public static final FeatureType<ItemDamageFeature> ITEM_DAMAGE = of("ITEM_DAMAGE", ItemDamageFeature.NO_OP);
	public static final FeatureType<KnockbackFeature> KNOCKBACK = of("KNOCKBACK", KnockbackFeature.NO_OP);
	public static final FeatureType<PotionFeature> POTION = of("POTION", PotionFeature.NO_OP);
	public static final FeatureType<BowFeature> BOW = of("BOW", BowFeature.NO_OP);
	public static final FeatureType<CrossbowFeature> CROSSBOW = of("CROSSBOW", CrossbowFeature.NO_OP);
	public static final FeatureType<FishingRodFeature> FISHING_ROD = of("FISHING_ROD", FishingRodFeature.NO_OP);
	public static final FeatureType<ItemProjectileFeature> ITEM_PROJECTILE = of("ITEM_PROJECTILE", ItemProjectileFeature.NO_OP);
	public static final FeatureType<TridentFeature> TRIDENT = of("TRIDENT", TridentFeature.NO_OP);
	public static final FeatureType<DifficultyProvider> DIFFICULTY = of("DIFFICULTY", DifficultyProvider.DEFAULT);
	public static final FeatureType<SpectateFeature> SPECTATE = of("SPECTATE", SpectateFeature.NO_OP);
	public static final FeatureType<TotemFeature> TOTEM = of("TOTEM", TotemFeature.NO_OP);
	public static final FeatureType<DeathMessageFeature> DEATH_MESSAGE = of("DEATH_MESSAGE", DeathMessageFeature.NO_OP);
	public static final FeatureType<TrackingFeature> TRACKING = of("TRACKING", TrackingFeature.NO_OP);
	
	public static <F extends CombatFeature> FeatureType<F> of(String name, F noopFeature) {
		return new FeatureType<>(name, noopFeature);
	}
}
