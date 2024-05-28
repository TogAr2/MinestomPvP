package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.feature.CombatFeature;

public interface BowFeature extends CombatFeature {
	BowFeature NO_OP = new BowFeature() {};
}
