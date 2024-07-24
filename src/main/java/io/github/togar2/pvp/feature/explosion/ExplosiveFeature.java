package io.github.togar2.pvp.feature.explosion;

import io.github.togar2.pvp.feature.CombatFeature;

/**
 * Combat feature which handles placing and igniting explosives.
 * Supports tnt, end crystals and respawn anchors.
 */
public interface ExplosiveFeature extends CombatFeature {
	ExplosiveFeature NO_OP = new ExplosiveFeature() {};
}
