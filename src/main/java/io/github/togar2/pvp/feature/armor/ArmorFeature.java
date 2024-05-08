package io.github.togar2.pvp.feature.armor;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;

public interface ArmorFeature extends CombatFeature {
	float getDamageWithProtection(LivingEntity entity, DamageType type, float amount);
}
