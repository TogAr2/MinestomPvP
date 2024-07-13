package io.github.togar2.pvp.feature.damage;

import io.github.togar2.pvp.feature.CombatFeature;

/**
 * Combat feature which handles entities being damaged.
 */
public interface DamageFeature extends CombatFeature {
	DamageFeature NO_OP = new DamageFeature() {};
}
