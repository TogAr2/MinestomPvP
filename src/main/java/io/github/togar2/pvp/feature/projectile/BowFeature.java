package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.feature.CombatFeature;

/**
 * Combat feature which handles bow shooting.
 */
public interface BowFeature extends CombatFeature {
	BowFeature NO_OP = new BowFeature() {};
}
