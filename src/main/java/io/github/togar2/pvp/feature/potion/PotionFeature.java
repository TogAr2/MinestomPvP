package io.github.togar2.pvp.feature.potion;

import io.github.togar2.pvp.feature.CombatFeature;

public interface PotionFeature extends CombatFeature {
	PotionFeature NO_OP = new PotionFeature() {};
}
